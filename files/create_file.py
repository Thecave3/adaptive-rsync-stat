import random
import string


name = "file_"
content = ''
for x in range(0, 1000):
    print('Creating file ' + name + str(x))
    file = open(name + str(x), 'w')
    if (x == 0):
        for i in range(0, 700000):
            content += random.choice(string.ascii_letters)
        file.write(content)
        file.close()
    else:
        randomString = ''
        for i in range(0, 100):
            randomString += random.choice(string.ascii_letters)
        last = open(name + str(x - 1), 'r')
        content = last.read()
        for x in range(0, 500):
            index = random.randint(0, 700000)
            content = content[:index] + randomString + content[index + 1:]
        print('Aggiunto la random string per 500 volte')
        for x in range(0, 500):
            index = random.randint(0, 699900)
            content = content[:index] + content[index + 100:]
        print('Tolte parti random per 500 volte')
        file.write(content)
        last.close()
        file.close()
