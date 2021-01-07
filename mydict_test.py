import unittest
from mydict import search_dict

class Test_mydict(unittest.TestCase):
    def test_search_dict(self):
        text = 'teddy bears on'
        meaning = search_dict(text)
        print(meaning)
    

if __name__ == '__main__':
   unittest.main() 

    
   