import os
from traceback import print_tb

x = input("Press Enter after ensuring all the services are up and running")
print("\n\nWhich set of test cases do you want to use")
print("1. Public Test Cases (Enter input 1)")
print("2. Self prepared Test Cases (Enter input 2)")
print("Any other key to exit")

choice = input()
if(choice.isnumeric):
    choice = int(choice)
    if choice == 1 or choice==2:
        if choice == 1:
            os.chdir("test-suites/public-test-cases")
        else:
            os.chdir("test-suites/self-test-cases")
        files = os.listdir()
        for f in files:
            if(f.endswith(".py")):
                print("\n\nTest Case : " + f)
                os.system("python3 " + f)
                x = input("Press any key to start the next test case")


    







