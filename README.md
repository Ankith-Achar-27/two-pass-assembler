# Two-Pass Assembler Simulation

A Java-based simulation of a **Two-Pass Assembler** for a SIC assembly language program. The project demonstrates how an assembler processes source code in two passes to generate object code and an object program.

## Project Objective

The objective of this project is to demonstrate the working of a **Two-Pass Assembler**, including:

- Symbol Table (SYMTAB) generation
- Literal Table (LITTAB) generation
- Location Counter (LOCCTR) management
- Intermediate file generation
- Object code generation
- Header, Text, and End (H/T/E) record generation

## Technologies Used

- **Java**
- **IntelliJ IDEA**
- **SIC Assembly Language**

## Input Program

The assembler processes the following SIC assembly program:

```asm
LITPRG  START  6000
        LDA    =X'05'
        ADD    =X'0A'
        STA    RESULT
        RSUB
RESULT  RESW   1
        END    LITPRG