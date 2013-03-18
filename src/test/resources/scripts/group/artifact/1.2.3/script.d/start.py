#!/usr/bin/python
#
# Simple Python test script to make sure we are able to test various
# script-running scenarios.

import sys
import argparse
import time

p = argparse.ArgumentParser()

# Required arguments
p.add_argument("--runtime-dir", help = "The runtime directory", required = True)

# Arguments used to simulate various behaviors
p.add_argument("--exit-success", help = "Exit with return value 0", action = 'store_true')
p.add_argument("--exit-fail", help = "Exit with given return value", nargs = 1, type = int, action = 'store')
p.add_argument("--sleep", help = "Sleep for given number of milliseconds", nargs = 1, type = int, action = 'store')

args = p.parse_args()

def main():
    if args.sleep:
        t = args.sleep[0] / 1000.0
        time.sleep(t)

    if args.exit_success:
        exit(0)

    if args.exit_fail:
        exit(args.exit_fail[0])


if __name__ == "__main__":
    main()
