bios = open('bios.bin', 'rb').read()
font = bios[-1426:-402]
letters = []
shifts = []
shift = 0

for i in range(128):
    cur = []
    for x in range(7, -1, -1):
        c = []
        for y in range(8):
            c.append((font[8*i+y]>>x)&1)
        cur.append(c)
    while len(cur) > 1 and cur[0] == [0, 0, 0, 0, 0, 0, 0, 0]: del cur[0]
    while len(cur) > 1 and cur[-1] == [0, 0, 0, 0, 0, 0, 0, 0]: del cur[-1]
    shifts.append(shift)
    shift += len(cur)
    for i in cur: letters.append(int(''.join(map(str, reversed(i))), 2))
    if i == 32:
        shift += 4
        letters.extend((0, 0, 0, 0))
shifts.append(shift)

print('package org.homebrew;')
print()
print('class FontBundle')
print('{')
print('    public static final byte[] letters = {'+', '.join(str(i if i < 128 else i-256) for i in letters)+'};')
print('    public static final int[] shifts = {'+', '.join(map(str, shifts))+'};')
print('}')
