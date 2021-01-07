from flask import Flask, request, jsonify
from mwe import get_MWE_at_offset, identify_MWE
from mydict import search_dict

app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello, World!'

@app.route('/MWE-dict', methods=['GET', 'POST'])
def MWE_dict():
    json_data = request.get_json()
    sent = json_data['sent']
    sel_offset = json_data['sel_offset']
    MWE = get_MWE_at_offset(sent, sel_offset)

    words = [word for word, _ in MWE]
    offsets = [offset for _, offset in MWE]

    joined_MWE = ' '.join(words)
    meaning = search_dict(joined_MWE)
    
    return {
        'MWE' : joined_MWE,
        'offsets' : offsets,
        'meaning' : meaning
    }

# @app.route('/all-MWEs')
# def MWEs():
#     json_data = request.get_json()
#     sent = json_data['sent']
#     model_outputs = identify_MWE(sent)
#     return model_outputs


    