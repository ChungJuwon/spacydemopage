import unittest
from mwe import identify_MWE, is_MWE, gather_MWEs, pick_MWE_at_offset, get_MWE_at_offset, get_word_by_token_index, get_MWE_at_offset

class Test_MWE(unittest.TestCase):

    def test_identify_mwe(self):
        sent = 'Crackdowns on illegal tattoo parlors have often led to the discovery of ink with excessive amounts of lead, cadmium, arsenic and other heavy metals.'
        correct_label = ['B', 'I', 'O', 'O', 'B', 'o', 'I']
        
        result = identify_MWE(sent)
        resultk = [[a,b,c] for a,b,c in result]
        print(resultk)
        print("$$$")

    def test_is_MWE(self):
        model_output = [('O', 'i', (0, 1)), ('O', 'love', (2, 6)), ('O', 'you', (7, 10))]   
        _is_MWE, token_index = is_MWE(3, model_output)
        self.assertEqual(_is_MWE, False)
        self.assertEqual(token_index, 1)

    def test_gather_MWEs(self):
        correct_MWEs = [[('B', 'of', (0, 2)), ('I', 'course', (3, 9))],
                        [('B', 'pull', (14, 18)), ('I', 'up', (24, 26))]
                    ]
        model_outputs = [['B', 'crack', (0, 5)], ['I', '##down', (5, 9)], ['I', '##s', (9, 10)], ['O', 'on', (11, 13)], ['O', 'illegal', (14, 21)], ['B', 'tattoo', (22, 28)], ['I', 'parlor', (29, 35)], ['O', '##s', (35, 36)], ['O', 'have', (37, 41)], ['O', 'often', (42, 47)], ['B', 'led', (48, 51)], ['I', 'to', (52, 54)], ['O', 'the', (55, 58)], ['O', 'discovery', (59, 68)], ['O', 'of', (69, 71)], ['O', 'ink', (72, 75)], ['O', 'with', (76, 80)], ['O', 'excessive', (81, 90)], ['O', 'amounts', (91, 98)], ['O', 'of', (99, 101)], ['O', 'lead', (102, 106)], ['O', ',', (106, 107)], ['O', 'cad', (108, 111)], ['O', '##mium', (111, 115)], ['O', ',', (115, 116)], ['O', 'arsenic', (117, 124)], ['O', 'and', (125, 128)], ['O', 'other', (129, 134)], ['B', 'heavy', (135, 140)], ['I', 'metals', (141, 147)], ['O', '.', (147, 148)]]
        MWEs = gather_MWEs(model_outputs)
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
        print(MWEs)
    
    def test_pick_MWE_at_offset(self):
        correct_MWE = [('of', (0, 2)), ('course', (3, 9))]

        MWEs = [[('B', 'of', (0, 2)), ('I', 'cour', (3, 7)), ('I', '##se', (7, 9))],
                [('B', 'pull', (14, 18)), ('I', 'up', (24, 26))]
               ]
        
        MWE = pick_MWE_at_offset(4, MWEs)
        self.assertEqual(MWE, correct_MWE)
    
    def test_get_word_by_token_index(self):
        correct_word = [('this', (19, 23))]
        
        model_outputs = [('B', 'of', (0, 2)), ('I', 'course', (3, 9)), ('O', ',', (9,10)),
                ('O', 'I', (11,12)), ('B', 'pull', (14, 18)), ('o', 't', (19, 20)), ('o', '##hi', (20, 22)), ('o', '##s', (22, 23)), ('I', 'up', (24, 26))
                ]

        word = get_word_by_token_index(6, model_outputs)
        self.assertEqual(correct_word, word)
    
    def test_get_MWE_at_offset(self):
        sent = 'I pulled this up!!!'
        for i in range(len(sent)-1):
            result = get_MWE_at_offset(sent, i)
            print(f'offset[{i}] : {result}')
            

if __name__ == '__main__':
    unittest.main()
