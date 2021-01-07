import logging
import torch
import numpy as np
import csv
from transformers import BertForTokenClassification, BertConfig
from tokenizers import BertWordPieceTokenizer

# Set device as GPU 
print(torch.__version__)
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
MAX_LENGTH = 100

# Load a trained model and vocabulary
output_dir = './model_save/'
model = BertForTokenClassification.from_pretrained(output_dir)
# model = BertForTokenClassification(BertConfig())
tokenizer = BertWordPieceTokenizer("bert-base-uncased-vocab.txt", lowercase=True)

# Copy the model to the GPU.
model.to(device)

#label_map = {'O':0, 'B':1, 'I':2, '0':3, 'o':4, 'b':5, 'i':6}
id_to_label = {0:'O', 1:'B', 2:'I', 3:'0', 4:'o', 5:'b', 6:'i'}
SEP = 102

def identify_MWE(sent):
    '''Identify labels of tokens in original sentence.
    Result does not contain special token(i.e. [CLS],[SEP],[PAD])
    
    Parameters:
    sent (string): sentence to predict

    Returns:
    list: ziped list of token_labels, tokens, offsets
    - token_labels (list): list BIO-tagged predictions of each tokens
    - tokens (list): list of token
    - offsets (list): offset of each token in original sentence

    '''
    encoding = tokenizer.encode(sent)
    tokens_len = len(encoding.ids)
    encoding.pad(MAX_LENGTH)

    pt_input_ids = torch.stack([torch.tensor(encoding.ids)], dim=0)
    pt_attention_masks = torch.stack([torch.tensor(encoding.attention_mask)], dim=0)
    
    pt_input_ids = pt_input_ids.to(device)
    pt_attention_masks = pt_attention_masks.to(device)
    
    model.eval()
    
    with torch.no_grad():
      outputs = model(pt_input_ids, token_type_ids=None,
                      attention_mask=pt_attention_masks)
    
    logits = outputs[0]
    logits = logits.detach().cpu().numpy()
    assert logits.shape == (1, MAX_LENGTH, len(id_to_label)+1), f'logit.shape: {logits.shape}'
    predicted_label_ids = np.argmax(logits, axis=2)
    assert predicted_label_ids.shape == (1, MAX_LENGTH)
    
    # strip special tokens([CLS][SEP][PAD])
    real_slice = slice(1, tokens_len-1)
    token_labels = predicted_label_ids[0][real_slice].tolist()
    token_labels = [id_to_label[id] for id in token_labels]

    return zip(token_labels, encoding.tokens[real_slice], encoding.offsets[real_slice])

def is_MWE(sel_offset, model_outputs):
    ''' determines whether a token at selected offset is element of MWE

    Parameters:
    sel_offset (int): a offset selected by user
    model_outputs (list): result of identify_MWE(), zip(token_labels, tokens, offsets)
    
    Returns:
    bool: True if selected offset is element of MWE
    int: idex of sel_offset in model_outputs
    
    '''    
    
    for i, (token_label, _, offset) in enumerate(model_outputs):
        if sel_offset in range(*offset):
            if token_label in ('B', 'b', 'I', 'i'):
                return True, i
            else:
                return False, i

def validate_sel_offset(sent, sel_offset):
    ''' returns nearest valid offset if chracter at offset is white space or special character

    Parameters:
    sent (string): a sentence
    sel_offset (int): a offset selected by user
    
    Returns:
    int: nearest valid offset, or None if sentence has no valid offset
    '''

    old_offset = sel_offset
    while sent[sel_offset] in ' \t.,!?`~!@#$%^&*()-_+=[{}]\\|:;\'\"<>/':
        sel_offset -= 1
        if sel_offset < 0:
            break
    
    if sel_offset >= 0:
        return sel_offset

    sel_offset = old_offset
    
    while sent[sel_offset] in ' \t.,!?`~!@#$%^&*()-_+=[{}]\\|:;\'\"<>/':
        sel_offset += 1
        if sel_offset >= len(sent):
            break
    
    if sel_offset < len(sent):
        return sel_offset
    
    return None


def get_MWE_at_offset(sent, sel_offset):
    ''' returns MWE(or word) at selected offset in sentence

    Parameters:
    sent (string): a sentence
    sel_offset (int): a offset selected by user
    
    Returns:
    list: a list containing tuples of words(merged tokens) and offset(also merges)
    '''
    sel_offset = validate_sel_offset(sent, sel_offset)
    assert sel_offset is not None, f'sentence has no valid token! sentence:{sent}'

    model_outputs = identify_MWE(sent)
    model_outputs = list(model_outputs)
    _is_MWE, token_index = is_MWE(sel_offset, model_outputs)
    if _is_MWE:
        MWEs = gather_MWEs(model_outputs)
        try:
            return pick_MWE_at_offset(sel_offset, MWEs)
        except AssertionError as error:
            logging.exception(error)
            return get_word_by_token_index(token_index, model_outputs)
    else:
        return get_word_by_token_index(token_index, model_outputs)

def pick_MWE_at_offset(sel_offset, MWEs):
    ''' get MWE which contains selected offset among MWEs
    
    Parameters:
    sel_offset (int): a offset selected by user
    MWEs (list): list of MWEs, where each MWE looks like [(B, (0,1)), (I, (3,4))]

    Returns:
    list: a list containing tuples of words(merged tokens) and offset(also merges)

    '''

    selected_MWE = None
    for mwe in MWEs:
        for _, _, offset in mwe:
           if sel_offset in range(*offset):
               selected_MWE = mwe 
               break
        if selected_MWE is not None:
            break
    
    assert selected_MWE is not None, f'selected_MWE should not be None, offset: {sel_offset}'
    
    # merge tokens to words
    words = []
    word = ''
    prev_offset = (-1, -1)
    for _, token, offset in selected_MWE:
        if offset[0] == prev_offset[1]:
            word += token[2:] if token[:2] == '##' else token
            prev_offset = (prev_offset[0], offset[1])
        else:
            if len(word) != 0:
                words.append((word, prev_offset))
            word = token
            prev_offset = offset
        
    words.append((word, prev_offset))

    

    return words


def get_word_by_token_index(token_index, model_outputs):
    ''' gets a word containing token at token_index in model_output
    
    Parameters:
    token_index (int): index of token in model_outputs
    model_output (list): output of identify_mwe(), zip(labels, tokens, offsets)
    
    returns:
    list: a list containing a tuple of word and offset of the word in original sentence
    '''
    
    word = '' 
    word_offset = (-1, -1)
    
    # backward search
    for _, token, offset in reversed(model_outputs[:token_index +1]):
        if token[:2] == '##':
            word = token[2:] + word
            word_offset = (offset[0], word_offset[1]) if word_offset != (-1, -1) else offset
        else:
            word = token + word
            word_offset = (offset[0], word_offset[1]) if word_offset != (-1, -1) else offset
            break
        
    # last word
    if token_index == len(model_outputs) -1:
        return [(word, word_offset)]

    # forward search
    for _, token, offset in model_outputs[token_index+1:]:
        if token[:2] == '##':
            word += token[2:]
            word_offset = (word_offset[0], offset[1])
        else:
            break
            
    return [(word, word_offset)]


def gather_MWEs(model_outputs):
    ''' gathers all MWEs in model_outputs

    Parameters:
    model_outs (list): result of identify_mwe(), zip(labels, tokens, offsets)
    
    Returns:
    list: a list of MWEs which is list containing tuples (label, token, offset)
    '''
    # MWEs = [ [(B, 'I', (0, 1)), (I, 'am', (2, 4))],
    #          [(B, 'an', (6, 8)), (I, 'jh', (9, 11))],
    #           [(b, 'me', (12, 14)), (i, 'ko', (16, 18))]
    #        ]
    MWEs = []
    BI = []
    bi = []
    for label, token, offset in model_outputs:
        try:
            if label == 'B':
                if len(BI) >0:
                    assert len(BI) != 1, f'B should be with I, B-offset: {offset}'
                    MWEs.append(BI)
                
                BI = []
                BI.append((label, token, offset))

            if label == 'b':
                if len(bi) >0:
                    assert len(bi) != 1, f'b should be with i, b-offset: {offset}'
                    MWEs.append(bi)
                
                bi = []
                bi.append((label, token, offset))
            
            if label == 'I':
                assert len(BI) > 0, f'I needs preceding B, I-offset: {offset}'
                if token[:2] == '##': 
                    # BI[-1][2][1] : end offset of last token in BI
                    assert BI[-1][2][1] == offset[0], f'I starts with ## should not be dangling, I-offset: {offset}' 

                BI.append((label, token, offset))
            
            if label == 'i':
                assert len(bi) > 0, f'i needs preceding b, i-offset: {offset}'
                if token[:2] == '##': 
                    # bi[-1][2][1] : end offset of last token in bi
                    assert bi[-1][2][1] == offset[0], f'i starts with ## should not be dangling, i-offset: {offset}' 
                
                bi.append((label, token, offset))

        except AssertionError as error:
            logging.exception(error)

    try:
        if len(BI) > 0:
            assert len(BI) != 1, f'len of BI should be more than 1, BI[0] : {BI[0]}'
            MWEs.append(BI)
        
        if len(bi) > 0:
            assert len(bi) != 1, f'len of bi should be more than 1, bi[0] : {bi[0]}'
            MWEs.append(bi)
            
    except AssertionError as error:
        logging.exception(error)

    return MWEs