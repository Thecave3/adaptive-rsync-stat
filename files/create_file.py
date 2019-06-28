import random
import string

ITERATIONS = 400

name = "file_"
content = ''
file_size = 500000
percentual_update = 2

for x in range(0, ITERATIONS):
    if (x%(int(ITERATIONS/10)) == 0):
        print('Status: '+str(int(x/(int(ITERATIONS/10)))*10)+'%')
    file = open(name + str(x) + ".edf", 'w')
    if (x == 0):
        for i in range(0, file_size):
            content += random.choice(string.ascii_letters)
        file.write(content)
        file.close()
    else:
        randomString = ''
        bytes_to_change = int(file_size / 100)
        for i in range(0, bytes_to_change):
            randomString += random.choice(string.ascii_letters)
        last = open(name + str(x - 1) + ".edf", 'r')
        content = last.read()
        for x in range(0, percentual_update):
            index = random.randint(0, file_size - bytes_to_change)
            content = content[:index] + randomString + content[index + bytes_to_change:]
        file.write(content)
        last.close()
        file.close()
        '''
        last = open(name + str(x - 1) + ".edf", 'r')
        content = last.read()
        bytes_to_change = int(file_size*percentual_update)
        for x in range(0,bytes_to_change):
            index = random.randint(0, file_size)
            content = content[:index] + random.choice(string.ascii_letters) + content[index + 2:]
            pass
        file.write(content)
        last.close()
        file.close()
        '''
print('Status: 100%')