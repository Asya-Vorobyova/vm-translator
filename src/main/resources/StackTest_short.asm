// push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
// push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
// eq
@SP
A=M-1
D=M
A=A-1
D=M-D
@YES3
D;JEQ
@ENDEQ3
@SP
M=M-1
A=M-1
M=0;JMP
(YES3)
@SP
M=M-1
A=M-1
M=-1
(ENDEQ3)
// push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
// push constant 16
@16
D=A
@SP
A=M
M=D
@SP
M=M+1
// eq
@SP
A=M-1
D=M
A=A-1
D=M-D
@YES6
D;JEQ
@ENDEQ6
@SP
M=M-1
A=M-1
M=0;JMP
(YES6)
@SP
M=M-1
A=M-1
M=-1
(ENDEQ6)
// push constant 16
@16
D=A
@SP
A=M
M=D
@SP
M=M+1