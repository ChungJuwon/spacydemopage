import os
import six
from google.cloud import translate_v2 as translate
# TODO : Delete this when deploy!!!
translate_client = translate.Client()

# def search_dict(text):
#     '''Search meaning of keyword
#     Parameters:
#     text (str): keyword to search
    
#     Returns:
#     str: meaning of keywoard
#     '''
#     return text + "_meaning"

def search_dict(text):
    '''Search meaning of keyword
    Parameters:
    text (str): keyword to search
    
    Returns:
    str: meaning of keywoard
    '''
    if isinstance(text, six.binary_type):
        text = text.decode('utf-8')
    
    # Text can also be a sequence of strings, in which case this method
    # will return a sequence of results for each text.
    result = translate_client.translate(
        text, target_language='ko', source_language='en')
    
    return result['translatedText']
    