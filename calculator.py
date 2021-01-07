from flask import Flask, render_template, request, Markup
import spacy
import re
from spacy import displacy
from graphviz import Digraph
from mwe import identify_MWE, is_MWE, gather_MWEs, pick_MWE_at_offset, get_MWE_at_offset, get_word_by_token_index, get_MWE_at_offset
from mydict import search_dict
nlp = spacy.load('en_core_web_sm')


sents = []
'''
finalsent = ""
alphabets= "([A-Za-z])"
prefixes = "(Mr|St|Mrs|Ms|Dr)[.]"
suffixes = "(Inc|Ltd|Jr|Sr|Co)"
starters = "(Mr|Mrs|Ms|Dr|He\s|She\s|It\s|They\s|Their\s|Our\s|We\s|But\s|However\s|That\s|This\s|Wherever)"
acronyms = "([A-Z][.][A-Z][.](?:[A-Z][.])?)"
websites = "[.](com|net|org|io|gov)"

def split_into_sentences(text):
    text = " " + text + "  "
    text = text.replace("\n"," ")
    text = re.sub(prefixes,"\\1<prd>",text)
    text = re.sub(websites,"<prd>\\1",text)
    if "Ph.D" in text: text = text.replace("Ph.D.","Ph<prd>D<prd>")
    text = re.sub("\s" + alphabets + "[.] "," \\1<prd> ",text)
    text = re.sub(acronyms+" "+starters,"\\1<stop> \\2",text)
    text = re.sub(alphabets + "[.]" + alphabets + "[.]" + alphabets + "[.]","\\1<prd>\\2<prd>\\3<prd>",text)
    text = re.sub(alphabets + "[.]" + alphabets + "[.]","\\1<prd>\\2<prd>",text)
    text = re.sub(" "+suffixes+"[.] "+starters," \\1<stop> \\2",text)
    text = re.sub(" "+suffixes+"[.]"," \\1<prd>",text)
    text = re.sub(" " + alphabets + "[.]"," \\1<prd>",text)
    if "”" in text: text = text.replace(".”","”.")
    if "\"" in text: text = text.replace(".\"","\".")
    if "!" in text: text = text.replace("!\"","\"!")
    if "?" in text: text = text.replace("?\"","\"?")
    text = text.replace(".",".<stop>")
    text = text.replace("?","?<stop>")
    text = text.replace("!","!<stop>")
    text = text.replace("<prd>",".")
    sentences = text.split("<stop>")
    sentences = sentences[:-1]
    sentences = [s.strip() for s in sentences]
    return sentences
'''
def draw_tree(root, get_children, get_print, get_label):
    queue = list()
    prev_depth = 0
    dot = Digraph(format="svg")
    queue.append((root, -1))
    global finalsent
    id = 0
    while len(queue) != 0:
        current, parent_id = queue.pop(0)
        dot.node(str(id), get_print(current))
        if parent_id != -1:
            dot.edge(str(parent_id), str(id), label=get_label(current))

        for child in get_children(current):
            queue.append((child, id))

        id += 1
    finalsent = finalsent + dot.pipe().decode('utf-8')+"<br>"

def draw_dep_tree(doc):
    i = -1

    # find ROOT
    for token in doc:
        if token.dep_ == 'ROOT':
            i = token.i
            break

    if i != -1:
        draw_tree(doc[i], 
            lambda current: current.children,
            lambda current: current.text,
            lambda current: current.dep_)

from spacy.tokens import Token
Token.set_extension('myparent', default=list(), force=True)

def find_root(doc):
    for token in doc:
        if token.dep_ == 'ROOT':
            return token
    return None

group_to_dep = {'subj':['csubj', 'nsubj', 'csubjpass', 'nsubjpass'], 
            'verb':['aux', 'neg', 'auxpass'],
            'obj': ['dative', 'dobj'],
            'comp': ['attr', 'acomp', 'ccomp', 'xcomp'],
            'aux': ['prep', 'pcomp', 'advcl', 'relcl', 'agent', 'advmod']}
            
dep_to_group = dict()
for k, vs in group_to_dep.items():
    for v in vs:
        dep_to_group[v] = k

recursive = set(['conj', 'advcl'])
verb_like = set(['VERB', 'AUX'])
def obj_handler(token):
    global finalsent
    span = token.doc[token.left_edge.i: token.right_edge.i+1]
    register_self_to_tokens(span)
    
    finalsent = finalsent + 'Here is obj'+' '+token.text+' '+f"{token.pos_}"+"<br>"

def subj_handler(token):
    global finalsent
    span = token.doc[token.left_edge.i: token.right_edge.i+1]
    register_self_to_tokens(span)
    finalsent = finalsent +'Here is subj'+' '+token.text+' '+f"{token.pos_}"+"<br>"

def comp_handler(token):
    global finalsent
    span = token.doc[token.left_edge.i: token.right_edge.i+1]
    register_self_to_tokens(span)
    finalsent = finalsent +'Here is comp'+' '+token.text+' '+f"{token.pos_}"+"<br>"

def aux_handler(token):
    global finalsent
    span = token.doc[token.left_edge.i: token.right_edge.i+1]
    register_self_to_tokens(span)
    finalsent = finalsent +'Here is aux'+' '+token.text+' '+f"{token.pos_}"+"<br>"

def dummy_handler(token):
    global finalsent
    # span = token.doc[token.left_edge.i: token.right_edge.i+1]
    # register_self_to_tokens(span)
    finalsent = finalsent +'Here is dummy'+' '+f"{token}"+' '+f"{token.dep_}"+"<br>"
handlers = {'subj':subj_handler, 'obj':obj_handler,
            'comp':comp_handler, 'aux': aux_handler}

def register_self_to_tokens(span):
    for token in span:
        token._.myparent.append(span)

def verb_handler(token):
    global finalsent
    finalsent = finalsent +'Here is verb '+token.text+"<br>"
    def expand_verb(token):
        global finalsent
        # TODO: 
        # - expand to verbal-MWE
        # - common subj, obj of multiple verb
        children_and_self = list(token.lefts) + [token] + list(token.rights)
        spans = []
        span = None
        for child in children_and_self:
            if dep_to_group.get(child.dep_, None) == 'verb' or child == token:
                finalsent = finalsent +'this is expand '+f"{child}"+"<br>"
                if span is None:
                    span = (child.i, child.i+1)
                elif span[1] == child.i:
                    span = (span[0], child.i+1)
                else:
                    spans.append(span)
                    span = (child.i, child.i+1)
        
        if span is not None:
            spans.append(span)
        
        for span in spans:
            v_span = token.doc[slice(*span)]
            # v_span._.mytag = 'verb'
            register_self_to_tokens(v_span)

    if token.pos_ in verb_like:
        expand_verb(token)

    for child in token.children:
        # print(child)
        group = dep_to_group.get(child.dep_, None)
        if group == 'verb':
            continue

        handlers.get(group, dummy_handler)(child)

        if child.dep_ in recursive:
            verb_handler(child)
#여기까지 설명
Flask_App = Flask(__name__) # Creating our Flask Instance

@Flask_App.route('/', methods=['GET'])
def index():
    """ Displays the index page accessible at '/' """

    return render_template('index.html')

@Flask_App.route('/operation_result/', methods=['POST'])
def operation_result():
    """Route where we send calculator form input"""
    global finalsent
    error = None
    result = None
    global sents
    finalsent = ""
    sents = []
    # request.form looks for:
    # html tags with matching "name= "
    first_input = request.form['Input1']
    if(not first_input):
        return render_template(
            'index.html',
            input1=first_input,
            result="No Input",
            calculation_success=True,
        )

    try:
        ##여기가 핵심!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        input1 = first_input
        doc1 = nlp(input1)
        sents =  [sent.text for sent in doc1.sents]

        # On default, the operation on webpage is addition
        for sent in sents:
            doc = nlp(sent)
            finalsent = finalsent+'<p style = "font-size:130%"><b>' + doc.text+"</b></p>"
            draw_dep_tree(doc)
            root = find_root(doc)
            if root.pos_ in verb_like:
                verb_handler(root)
            else:
                finalsent = finalsent+"something wrong"+"<br>"
                pass
            finalsent = finalsent+"==============="+"<br>"
            finalsent = finalsent+"<table>"
            for token in doc:
                finalsent = finalsent+"<tr>"+"<td>"+f"{token}"+"</td>"+"<td>"+f"{token._.myparent}"+"</td>"+"</tr>"
            finalsent = finalsent+"</table>"
            finalsent = finalsent+"==============="+"<br>"
            result = identify_MWE(sent)
            finalsent = finalsent+"<table>"
            for a, b, _ in result:
                finalsent = finalsent+"<tr>"+"<td>"+b+"</td>"+"<td>"+a+"</td>"+"</tr>"
            finalsent = finalsent+"</table>"
            finalsent = finalsent+"==============="+"<br>"
            result = identify_MWE(sent)
            MWEs = gather_MWEs(result)
            finalsent = finalsent+"<table>"
            for MWEtoken in MWEs:
                MWE = ""
                for _,MWEtokentoken,_ in MWEtoken:
                    if(MWEtokentoken[0]=='#'):
                        if(MWEtokentoken[1]=='#'):
                            if(MWE!=""):
                                MWE = MWE[:-1]
                            k = MWEtokentoken[2:]
                            MWE = MWE + k + " "
                        else:
                            MWE = MWE + MWEtokentoken + " "
                    else:
                        MWE = MWE + MWEtokentoken + " "
                MWE = MWE[:-1]
                print(MWE)
                meaning = search_dict(MWE)
                finalsent = finalsent+"<tr>"+"<td>"+MWE+"</td>"+"<td>"+meaning+"</td>"+"</tr>"
            finalsent = finalsent+"</table>"


        result = finalsent
        ##여기가 핵심!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        return render_template(
            'index.html',
            input1=input1,
            result=Markup(result),
            calculation_success=True
        )
        
    except ValueError:
        return render_template(
            'index.html',
            input1=first_input,
            result="Bad Input",
            calculation_success=False,
            error="Cannot perform numeric operations with provided input"
        )

if __name__ == '__main__':
    Flask_App.debug = True
    Flask_App.run(host='0.0.0.0')
