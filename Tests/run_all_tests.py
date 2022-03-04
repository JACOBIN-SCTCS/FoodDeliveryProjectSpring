import os

x = input("Press Enter after ensuring all the services are up and running")
files  = os.listdir()
for f in files:
    if(f.endswith(".py") and f not in ['run_all_tests.py','run_tests.py'] ):
        print("\n\nTest Case : " + f)
        os.system("python3 " + f)
        x = input("Press any key to start the next test case")


    







