import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class CodeWriter implements AutoCloseable {
    private final BufferedWriter writer;
    private String fileName;
    private String functionName = "";

    public CodeWriter(Path outputPath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputPath.toFile()));
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.functionName = fileName + ".none";
    }

    public void writeNewFile() throws IOException {
        oneLine("// " + fileName);
        writer.newLine();
    }

    public void writeInit() throws IOException {
        //TODO
    }

    public void writeArithmetic(String command, String commandSource, int commandNumber) throws IOException {
        oneLine("// " + commandSource);
        ArithmeticOp op = ArithmeticOp.parse(command);
        switch (Objects.requireNonNull(op)) {
            case neg:
            case not:
                unary(op);
                break;
            default:
                binary(op, commandNumber);
        }
    }

    public void writePushPop(CommandType command, String commandSource, String segment, int index, int commandNumber) throws IOException {
        oneLine("// " + commandSource);
        if (command == CommandType.C_PUSH) {
            applyPush(segment, index);
        } else {
            applyPop(segment, index, commandNumber);
        }
    }

    public void writeLabel(String label, String commandSource) throws IOException {
        oneLine("// " + commandSource);
        oneLine("(" + functionName + "$" + label + ")");
    }

    public void writeGoto(String label, String commandSource) throws IOException {
        oneLine("// " + commandSource);
        oneLine("@" + functionName + "$" + label);
        oneLine("0;JMP");
    }

    public void writeIf(String label, String commandSource) throws IOException {
        oneLine("// " + commandSource);
        assignDToStackValue();
        oneLine("@" + functionName + "$" + label);
        oneLine("D;JNE");
    }

    public void writeCall(String functionName, int numArgs, String commandSource) throws IOException {
        //TODO
    }

    public void writeReturn(String commandSource) throws IOException {
        oneLine("// " + commandSource);
        oneLine("@LCL");
        oneLine("D=M");
        oneLine("@" + this.fileName + "." + functionName + ".endFrame");
        oneLine("M=D"); // endFrame=LCL

        oneLine("@" + 5);
        oneLine("D=D-A"); // D=endFrame-5
        oneLine("A=D");
        oneLine("D=M"); // D=*(endFrame-5)
        oneLine("@" + this.fileName + "." + functionName + ".retAddr");
        oneLine("M=D"); // retAddr=*(endFrame-5)

        assignDToStackValue(); // D=pop()
        oneLine("@ARG");
        oneLine("A=M");
        oneLine("M=D"); // *ARG=pop()
        oneLine("D=A");
        oneLine("@SP");
        oneLine("M=D+1"); // SP=ARG+1

        restoreMemorySegment(MemorySegment.THAT); // THAT=*(endFrame-1)
        restoreMemorySegment(MemorySegment.THIS); // THIS=*(endFrame-2)
        restoreMemorySegment(MemorySegment.ARG); // ARG=*(endFrame-3)
        restoreMemorySegment(MemorySegment.LCL); // LCL=*(endFrame-4)

        oneLine("@" + this.fileName + "." + functionName + ".retAddr");
        oneLine("A=M");
        oneLine("0;JMP"); // goto retAddr
    }

    private void restoreMemorySegment(MemorySegment segment) throws IOException {
        oneLine("@" + this.fileName + "." + functionName + ".endFrame");
        oneLine("M=M-1"); // endFrame--
        oneLine("D=M"); // D=endFrame
        oneLine("A=D");
        oneLine("D=M"); // D=*endFrame
        oneLine("@" + segment.name());
        oneLine("M=D"); // SEG=*endFrame
    }

    public void writeFunction(String functionName, int numLocals, String commandSource) throws IOException {
        this.functionName = functionName;
        oneLine("// " + commandSource);
        oneLine("(" + functionName + ")");
        for (int i = 0; i < numLocals; i++) {
            applyPush(MemorySegment.CONST.getValue(), 0);
        }
    }

    public void close() throws IOException {
        writer.close();
    }

    private void applyPush(String segment, int index) throws IOException {
        assignDToRam(segment, index); //D equals a value to push
        oneLine("@SP");
        oneLine("A=M");
        oneLine("M=D"); //RAM[SP]=D
        oneLine("@SP");
        oneLine("M=M+1"); //SP++
    }

    private void applyPop(String segment, int i, int commandNumber) throws IOException {
        MemorySegment seg = MemorySegment.fromValue(segment);
        switch (Objects.requireNonNull(seg)) {
            case LCL:
            case ARG:
            case THIS:
            case THAT:
            case TEMP:
                assignDToAddress(seg, i);
                oneLine("@" + this.fileName + ".addr" + commandNumber);
                oneLine("M=D"); //addr=SEG+i / 5+i
                assignDToStackValue();
                oneLine("@" + this.fileName + ".addr" + commandNumber);
                oneLine("A=M");
                oneLine("M=D"); //RAM[addr]=RAM[SP]
                break;
            case STATIC:
                assignDToStackValue();
                oneLine("@" + this.fileName + "." + i);
                oneLine("M=D"); //Xxx.i=RAM[SP]
                break;
            case POINTER:
                assignDToStackValue();
                if (i == 0) {
                    oneLine("@" + MemorySegment.THIS.name());
                } else {
                    oneLine("@" + MemorySegment.THAT.name());
                }
                oneLine("M=D"); //THIS/THAT=RAM[SP]
        }
    }

    private void assignDToStackValue() throws IOException {
        oneLine("@SP");
        oneLine("M=M-1"); //SP--
        oneLine("A=M");
        oneLine("D=M"); //D=RAM[SP]
    }

    private void assignDToRam(String segment, int i) throws IOException {
        MemorySegment seg = MemorySegment.fromValue(segment);
        switch (Objects.requireNonNull(seg)) {
            case CONST:
                oneLine("@" + i);
                oneLine("D=A"); //D=i
                break;
            case LCL:
            case ARG:
            case THIS:
            case THAT:
            case TEMP:
                assignDToAddress(seg, i);
                oneLine("A=D");
                oneLine("D=M"); //D=RAM[SEG+i] / RAM[5+i]
                break;
            case STATIC:
                oneLine("@" + this.fileName + "." + i);
                oneLine("D=M"); //D=Xxx.i
                break;
            case POINTER:
                if (i == 0) {
                    oneLine("@" + MemorySegment.THIS.name());
                } else {
                    oneLine("@" + MemorySegment.THAT.name());
                }
                oneLine("D=M"); //D=THIS/THAT
        }
    }

    private void assignDToAddress(MemorySegment seg, int i) throws IOException {
        oneLine("@" + i);
        oneLine("D=A");
        oneLine("@" + (seg == MemorySegment.TEMP ? "5" : seg.name()));
        oneLine("D=D+" + (seg == MemorySegment.TEMP ? "A" : "M")); //D=SEG+i / 5+i
    }

    private void binary(ArithmeticOp op, int commandNumber) throws IOException {
        oneLine("@SP");
        oneLine("A=M-1");
        oneLine("D=M");
        oneLine("A=A-1"); //now A addresses x
        //we rewrite current stack value with op result
        switch (op) {
            case sub:
                oneLine("M=M" + op.getSymbol() + "D");
                oneLine("@SP");
                oneLine("M=M-1");
                break;
            case eq:
            case lt:
            case gt:
                comparison(op, commandNumber);
                break;
            case add:
            case and:
            case or:
                oneLine("M=D" + op.getSymbol() + "M");
                oneLine("@SP");
                oneLine("M=M-1");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + op);
        }
    }

    private void unary(ArithmeticOp op) throws IOException {
        oneLine("@SP");
        oneLine("A=M-1");
        oneLine("M=" + op.getSymbol() + "M");
    }

    private void comparison(ArithmeticOp op, int commandNumber) throws IOException {
        oneLine("D=M-D");
        oneLine("@" + this.fileName + ".YES" + commandNumber);
        oneLine("D;" + jump(op));
        oneLine("@SP");
        oneLine("M=M-1");
        oneLine("A=M-1");
        oneLine("M=0");
        oneLine("@" + this.fileName + ".ENDEQ" + commandNumber);
        oneLine("0;JMP");
        oneLine("(" + this.fileName + ".YES" + commandNumber + ")");
        oneLine("@SP");
        oneLine("M=M-1");
        oneLine("A=M-1");
        oneLine("M=-1");
        oneLine("(" + this.fileName + ".ENDEQ" + commandNumber + ")");
    }

    private String jump(ArithmeticOp op) {
        switch (op) {
            case eq:
                return "JEQ";
            case lt:
                return "JLT";
            case gt:
                return "JGT";
            default:
                throw new IllegalStateException("Unexpected value: " + op);
        }
    }

    private void oneLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }
}
