import random
import string

ITERATIONS = 200

name = "file_"
content = ''
file_size = 7000
percentual_update = 0.4

for x in range(0, ITERATIONS):
    if (x%(int(ITERATIONS/10)) == 0):
        print('Status: '+str(x/10)+'%')
    file = open(name + str(x) + ".edf", 'w')
    if (x == 0):
        for i in range(0, file_size):
            content += random.choice(string.ascii_letters)
        file.write(content)
        file.close()
    else:
        '''
        randomString = ''
        bytes_to_change = int(file_size / 100)
        times_to_update = int(percentual_update/2)

        for i in range(0, bytes_to_change):
            randomString += random.choice(string.ascii_letters)
        last = open(name + str(x - 1) + ".edf", 'r')
        content = last.read()
        for x in range(0, times_to_update):
            index = random.randint(0, file_size)
            content = content[:index] + randomString + content[index + 1:]
        # print('Aggiunto la random string per 500 volte')
        for x in range(0, times_to_update):
            index = random.randint(0, file_size - bytes_to_change)
            content = content[:index] + content[index + bytes_to_change:]
        # print('Tolte parti random per 500 volte')
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
