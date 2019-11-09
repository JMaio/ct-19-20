import os
from anytree import Node, RenderTree
from anytree.exporter import UniqueDotExporter as dot_exp


p = os.path.dirname(os.path.realpath(__file__))
f = "ast.c-ast"
filepath = os.path.join(p, "..", "tests", f)

ast_string = ""

with open(filepath) as file:
    ast_string = file.read()
# sample command for ast:
# java -cp bin Main -[mode] tests/fibonacci.c f.out

# contents = []
# while True:
#     try:
#         line = input().strip()
#     except EOFError:
#         break
#     contents.append(line)

contents = ast_string.strip().split()

print(contents)
ast = "".join(contents) #.split())
print(ast)
# "Program(Fundecl(void,f,Block(VarDecl(INT, a), Stmt())))"

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
        curr += 1
        if t == '(':
            # parse from here
            ps = parse(s[curr:])
            content.append(ps)

            if ps: # add offset of tokens to skip
                curr += len(flatten(ps))
        elif t == ')':
            return content

    return content


# print(lex);


p = parse(lex)
# print(p)

def sanitize(i):
    return [sanitize(t) if type(t) == list else t for t in i if t not in ['(', ',', ')']]


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

root = to_ast(s)

print(RenderTree(root))

dot_exp(root).to_dotfile("output.dot")
