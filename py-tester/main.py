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

test_result = ["FAIL", "PASS"]


def run_test(mode, filename, expected, out="a.out", logfile="test.log"):
    with open(logfile, 'a') as f:
        f.write(f" {filename} [{expected}]\n"), f.flush()
        code = subprocess.run(['java', '-cp', 'bin', 'Main',
                               f'-{mode}', f'tests/{filename}', out], stdout=f).returncode
        # print(f"{filename}: ({expected}) => {code}")
        f.write("\n")
        return code


def run_tests(mode, tests, logfile):
    failures = 0

    logging.info(f"====== running tests for: {mode} [ {len(tests)} ] ====== ")

    with open(logfile, 'a') as f:
        f.write(f"====== {mode} [ {len(tests)} ] ====== " + '\n')

    for i, (f, c) in enumerate(tests):
        logging.info(f" {i+1:2d} ─┬─[{c:3d}] {f}")
        code = run_test(mode, f, c, logfile=logfile)
        result = c == code
        failures += int(not result)
        
        logging.info(f"     └─[{code:3d}] {test_result[int(result)]}! ")
        logging.info("")

    logging.info(f" {mode}: - [ {len(tests) - failures} / {len(tests)} ] ")
    logging.info("")

    return failures


if __name__ == "__main__":
    filename = "tests.csv"
    modes = [
        "lexer",
        "parser"
    ]
    dir_path = os.path.dirname(os.path.realpath(__file__))

    now = datetime.datetime.now()
    logfile = f"ct-test-{now.strftime('%Y%m%d-%H%M%S')}.log"

    global_tests = 0
    global_fails = 0

    for mode in modes:
        tests = []
        file = os.path.join(dir_path, f"{mode}-{filename}")
        with open(file) as f:
            for line in f.readlines():
                f, c = line.strip().split(',')
                tests.append([f, codes[c.strip()]])

        global_tests += len(tests)
        global_fails += run_tests(mode, tests, logfile=logfile)
    
    logging.info("_______________________________________________________")
    logging.info("")
    logging.info(f" => all tests completed!")
    logging.info(f"    └── [ {global_tests - global_fails} / {global_tests} ] passed")


    # unittest.main(verbosity=2)
