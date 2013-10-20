package lexer;

import java.io.InputStream;
import java.util.regex.*;
import java.util.HashMap;
import java.util.Map;

import util.Todo;

import lexer.Token.Kind;

public class Lexer {
	
	String fname; // the input file name to be compiled
	
	InputStream fstream; // input stream for the above file
	
	int linenum;
	
	int columnum;
	
	Map<String, Kind> keywordMap = new HashMap<String, Kind>(); // store keyword and kind pairs

	public Lexer(String fname, InputStream fstream) {
		
		this.fname = fname;
		
		this.fstream = fstream;
		
		linenum = 1; //initial the linenum
		
		columnum = 0; //initial the columnnum

		// construct the keywords'hashmap
		keywordMap.put("boolean", Kind.TOKEN_BOOLEAN);
		
		keywordMap.put("class", Kind.TOKEN_CLASS);
		
		keywordMap.put("else", Kind.TOKEN_ELSE);
		
		keywordMap.put("extends", Kind.TOKEN_EXTENDS);
		
		keywordMap.put("false", Kind.TOKEN_FALSE);
		
		keywordMap.put("if", Kind.TOKEN_IF);
		
		keywordMap.put("int", Kind.TOKEN_INT);
		
		keywordMap.put("length", Kind.TOKEN_LENGTH);
		
		keywordMap.put("main", Kind.TOKEN_MAIN);
		
		keywordMap.put("new", Kind.TOKEN_NEW);
		
		keywordMap.put("out", Kind.TOKEN_OUT);
		
		keywordMap.put("println", Kind.TOKEN_PRINTLN);
		
		keywordMap.put("public", Kind.TOKEN_PUBLIC);
		
		keywordMap.put("return", Kind.TOKEN_RETURN);
		
		keywordMap.put("static", Kind.TOKEN_STATIC);
		
		keywordMap.put("String", Kind.TOKEN_STRING);
		
		keywordMap.put("System", Kind.TOKEN_SYSTEM);
		
		keywordMap.put("this", Kind.TOKEN_THIS);
		
		keywordMap.put("true", Kind.TOKEN_TRUE);
		
		keywordMap.put("void", Kind.TOKEN_VOID);
		
		keywordMap.put("while", Kind.TOKEN_WHILE);
	}

	// When called, return the next token (refer to the code "Token.java")
	// from the input stream.
	// Return TOKEN_EOF when reaching the end of the input stream.
	private Token nextTokenInternal() throws Exception {
		
		int c = this.fstream.read();
		
		columnum++;
		if (-1 == c)
			// The value for "lineNum" is now "null",
			// you should modify this to an appropriate
			// line number for the "EOF" token.
			return new Token(Kind.TOKEN_EOF, 0, 0);

		// skip all kinds of "blanks"
		while (' ' == c || '\t' == c || '\n' == c || '\r' == c) {
			
			if (c == '\t'){
				
				columnum = columnum + 4;
			}	
			else{
				
				columnum++;
			}
			
			c = this.fstream.read();
			
			if ('\n' == c) { // start a new line ,the colunnum need to be set zero.
				
				linenum++;
				
				columnum = 0;
			}
		}
		
		columnum++;
		
		if (-1 == c)
			return new Token(Kind.TOKEN_EOF, linenum, 0);
		
        // deal with the tokens for operators or symbols
		switch (c) {
		
		case '+':
			return new Token(Kind.TOKEN_ADD, linenum, columnum, "+");
			
		case '-':
			return new Token(Kind.TOKEN_SUB, linenum, columnum, "-");
			
		case '*':
			return new Token(Kind.TOKEN_TIMES, linenum, columnum, "*");
			
		case '&':
			this.fstream.mark(1);  //make a mark which can help us back to the previous one
			
			c = this.fstream.read();
			
			columnum++;
			
			if (c == '&') {
				
				return new Token(Kind.TOKEN_AND, linenum, columnum, "&&");
				
			} else {
				
				this.fstream.reset();// help us back to the location that we  marked before
				
				columnum--;
			}

		case '=':
			return new Token(Kind.TOKEN_ASSIGN, linenum, columnum, "=");

		case ',':
			return new Token(Kind.TOKEN_COMMER, linenum, columnum, ",");

		case ';':
			return new Token(Kind.TOKEN_SEMI, linenum, columnum, ";");

		case '.':
			return new Token(Kind.TOKEN_DOT, linenum, columnum, ".");

		case '{':
			return new Token(Kind.TOKEN_LBRACE, linenum, columnum, "{");

		case '}':
			return new Token(Kind.TOKEN_RBRACE, linenum, columnum, "}");

		case '[':
			return new Token(Kind.TOKEN_LBRACK, linenum, columnum, "[");
			
		case ']':
			return new Token(Kind.TOKEN_RBRACK, linenum, columnum, "]");
			
		case '(':
			return new Token(Kind.TOKEN_LPAREN, linenum, columnum, "(");
			
		case ')':
			return new Token(Kind.TOKEN_RPAREN, linenum, columnum, ")");

		case '<':
			return new Token(Kind.TOKEN_LT, linenum, columnum, "<");

		case '!':
			return new Token(Kind.TOKEN_NOT, linenum, columnum, "!");
		
		//deal with comments
		case '/':
			fstream.mark(1);
			
			c = fstream.read();
			
			columnum++;
			
			if (c == '/') {
				
				while (c != '\n') {
					
					c = fstream.read();
					
					columnum++;
					
					if (-1 == c)
						return new Token(Kind.TOKEN_EOF, linenum, 0);
				}
				
				linenum++;
				
				columnum = 0;
				
				return nextToken();
				
			} else if (c == '*') {
				
				while (true) {
					c = fstream.read();
					
					if (c == '\n')
						
						linenum++;
					
					if (-1 == c)
						
						return new Token(Kind.TOKEN_EOF, linenum, columnum);
					
					if (c == '*') {
						
						fstream.mark(1);
						
						c = fstream.read();
						
						columnum++;
						
						if (c == '/') {
							
							return nextToken();
							
						} else {
							
							fstream.reset();
							
							columnum--;
						}
					}
				}
			}

		//deal with tokens for keywords or identifers or numbers	
		default:
			
			String sequence = "";
			
			String temp = String.valueOf((char) c);
			
			// first we treat all of them(keywords identifers and numbers) as strings
			while (Pattern.matches("\\w", temp)) {
				
				sequence += temp;
				
				fstream.mark(1);
				
				c = fstream.read();
				
				columnum++;
				
				temp = String.valueOf((char) c);
			}
			
			//deal with illegal symbols
			if (sequence.length() > 0) {
				
				fstream.reset();
				
				columnum--;
				
			} else{
				
				sequence = temp;
			}
			
			//check keywords by keyword's hashtable and produce tokens for keywords.
			if (keywordMap.containsKey(sequence)) {
				
				return new Token(keywordMap.get(sequence), linenum, columnum,
						sequence);
			}
			
			//match identifers by regular expression and produce tokens for identifers
			if (Pattern.matches("[a-zA-Z_][\\w]*", sequence)) {
				
				return new Token(Kind.TOKEN_ID, linenum, columnum, sequence);
			}
			
			//match numbers by regular expression and produce tokens for numbers
			if (Pattern.matches("[0-9]+", sequence)) {
				
				return new Token(Kind.TOKEN_NUM, linenum, columnum, sequence);
			}
			
			// outprint error
			System.out.println("Lex Error: " + sequence + " at line " + linenum
					+ " column " + columnum + ".");
			
			//if we want to continue lexer and find all the errors when lexer 
			//finds the first error, we need to return nextToken() 
			
			return nextToken();
			// Lab 1, exercise 2: supply missing code to
			// lex other kinds of tokens.
			// Hint: think carefully about the basic
			// data structure and algorithms. The code
			// is not that much and may be less than 50 lines. If you
			// find you are writing a lot of code, you
			// are on the wrong way.
			// new Todo();
		}
	}

	public Token nextToken() {
		Token t = null;

		try {
			t = this.nextTokenInternal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (control.Control.lex)
			System.out.println(t.toString());
		return t;
	}
}
