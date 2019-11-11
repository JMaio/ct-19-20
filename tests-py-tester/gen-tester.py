import unittest
import subprocess
import os
import logging
import datetime

logging.basicConfig(level=logging.DEBUG, format='%(message)s')

codes = {
    'FILE_NOT_FOUND': 2,
    'MODE_FAIL':      254,
    'LEXER_FAIL':     250,
    'PARSER_FAIL':    245,
    'SEM_FAIL':       240,
    'PASS':           0,
}

test_result = ["FAIL", ""]


def run_test(mode, filename, expected, out="main.asm", logfile="test.log", inp=''):
    with open(logfile, 'a') as f:
        f.write(f"[{expected:3d}] {filename} \n"), f.flush()
        proc = subprocess.run(
            ['java',  '-jar', 'desc/part3/Mars4_5.jar', 'sm', 'nc', 'me', f'tests/gen/asm/{filename}'],
            capture_output=True,
            # stdout=f,
            input="\n".join(inp).encode('ascii'),
        )

        f.write(6*" " + proc.stdout.decode('ascii') + '\n'), f.flush()

        # code = subprocess.run(['java', '-cp', 'bin', 'Main',
        #                        f'-{mode}', f'tests/{filename}', f'{filename}-dump'],
                            #    stdout=f, stderr=f).returncode
        # print(f"{filename}: ({expected}) => {code}")
        # print(proc.stdout)
        code = proc.returncode
        f.write(f"[{code:3d}] ----------------------------")
        f.write(2 * "\n")
        
        return proc


def run_tests(mode, tests, logfile):
    failures = 0

    logging.info(f"====== running tests for: {mode} [ {len(tests)} ] ====== \n")

    with open(logfile, 'a') as f:
        f.write(f"====== {mode} [ {len(tests)} ] ====== \n")

    for i, (f, c, inp, real) in enumerate(tests):
        proc = run_test(mode, f'{f[:(f.index("."))]}.asm', c, logfile=logfile, inp=inp)
        code = proc.returncode

        #                                 stdout is a bytestring
        result = (c == code) and (real == proc.stdout.decode('ASCII'))
        failures += int(not result)
        
        logging.info(f"{i+1:2d}─[{c:3d}]─>[{code:3d}] {test_result[int(result)]:4} {f}")

    logging.info(f"\n {mode}: - [ {len(tests) - failures} / {len(tests)} ] \n")

    return failures


def compile_all(tests):
    errors = 0
    for (f, c, inp, real) in tests:
        proc = subprocess.run(['java', '-cp', 'bin', 'Main', '-gen', f'tests/gen/{f}', f'tests/gen/asm/{f[:(f.index("."))]}.asm'])
        # all files should compile!
        if(proc.returncode != 0):
            print(f"'{f}' failed to compile!");
            errors += 1
    return errors


if __name__ == "__main__":
    filename = "tests.csv"
    modes = [
        "gen",
    ]
    dir_path = os.path.dirname(os.path.realpath(__file__))

    now = datetime.datetime.now()
    logfile = f"ct-gen-test-{now.strftime('%Y%m%d-%H%M%S')}.log"

    global_tests = 0
    global_fails = 0

    for mode in modes:
        tests = []
        file = os.path.join(dir_path, f"{mode}-{filename}")
        with open(file) as f:
            for line in f.readlines():
                l = line.strip()
                if l:
                    f, c, inp, real = l.split(',')
                    tests.append([f, codes[c.strip()], inp.strip().split(';'), real.strip()])

        if compile_all(tests) > 0:
            exit()

        global_tests += len(tests)
        global_fails += run_tests(mode, tests, logfile=logfile)
    
    logging.info("_______________________________________________________\n")
    logging.info(f" => all tests completed!")
    logging.info(f"    └── [ {global_tests - global_fails} / {global_tests} ] passed")


    # unittest.main(verbosity=2)
