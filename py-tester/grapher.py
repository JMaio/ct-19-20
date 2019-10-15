import os
from anytree import Node, RenderTree

p = os.path.dirname(os.path.realpath(__file__))
f = "ast.c-ast"
filepath = os.path.join(p, "..", "tests", f)

ast_string = ""

with open(filepath) as file:
    ast_string = file.read()


ast = "Program(Fundecl(void,f,Block(VarDecl(INT, a), Stmt())))"

n = ast.replace("(", " ( ").replace(")", " ) ").replace(",", " , ")

lex = n.split()

# https://rosettacode.org/wiki/Flatten_a_list#Python
def flatten(lst):
	return sum( ([x] if not isinstance(x, list) else flatten(x)
		     for x in lst), [] )

def parse(s):
    curr = 0
    
    content = []
    
    while curr < len(s):
        t = s[curr]

        # keep track of number of tokens consumed
        content.append(t)
        if t == '(':
            # parse from here
            ps = parse(s[curr+1:])
            content.append(ps)
            #print(ps)
            if ps:
                # flattened = [val for sublist in ps for val in sublist]
                # print(s[curr-1] + " ==> " + str(flatten(ps)))
                curr += len(flatten(ps)) + 1
            # else:
            #     curr += 1
        elif t == ')':
            return content
        else:
            curr += 1

        # if t == ',':
        #     pass
            # curr += 1
        # else:
        #     pass
            # content.append(t)
    return content


# print(lex);


p = parse(lex)
# print(p)

def sanitize(i):
    o = [sanitize(t) if type(t) == list else t for t in i if t not in ['(', ',', ')']]
    # o = [t for t in i if t not in ['(', ',', ')']]
    # o = [sanitize(t) if type(t) == list else t for t in o]
    return o


def to_ast(i, p=None):
    if not p and i:
        fst, n = i
        p = Node(fst)
        to_ast(n, p)
        return p
    else:
        # each token
        last_node = None
        for tok in i:
            if type(tok) == list:
                to_ast(tok, last_node)
            else:
                last_node = Node(tok, parent=p)

s = sanitize(p)
print(s)

print(RenderTree(to_ast(s)))

# d = f"dirgraph ast {{ {to_ast(s)} }}"
# print(d)



# for p in parse(lex):
#     pass
#     print(p)


# from nltk import Tree
# t = Tree.fromstring(ast_string.replace(',', ' '))

# t.pretty_print()
# with open("pretty_tree.txt", 'w') as f:
#     f.write()

# def strip_paren(s):
#     left  = s.find('(')
#     right = s.find(')')

#     if left > right:
#         # start finishing
#         a = s[:left]
#         return
#     else:
#         return [s[:left], strip_paren(s[left+1:]) ]


# def find_parens(s):
#     toret = {}
#     pstack = []

#     for i, c in enumerate(s):
#         if c == '(':
#             pstack.append(i)
#         elif c == ')':
#             if len(pstack) == 0:
#                 raise IndexError("No matching closing parens at: " + str(i))
#             toret[pstack.pop()] = i

#     if len(pstack) > 0:
#         raise IndexError("No matching opening parens at: " + str(pstack.pop()))

#     return toret
# def re_strip(s : str):
#     start = s.find('(')
#     end = s.rfind(')')
#     if start == -1 or end == -1:
#         return
#     a = s[:start]

#     return [a, re_strip(s[start+1:end-1])]

# t = Node("AST")
# lastNode = t

# parens = find_parens(ast_string).sorted()

# for i in range(parens):
#     try:
#         s = parens[i]
#         e = parens[i+1]
#         for node in ast_string[s:e].split(','):
            
#     except Exception as e:
#         pass
#     n = Node(ast_string[:b], parent=t)
#     lastNode = n

# rem = ast_string
# l = []
# for (s, e) in list(find_parens(ast_string).items())[::-1]:
#     l.append(ast_string[s+1:e])

# for a in l:
#     print(a)

# n = ast_string.replace('(', ':\n\t').replace(')', '\n').replace(',', '\n\t')
# print(n)
