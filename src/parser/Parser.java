package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser {
	Lexer lexer;
	Token current;
	Token temp;

	public Parser(String fname, java.io.InputStream fstream) {
		lexer = new Lexer(fname, fstream);
		current = lexer.nextToken();
	}

	// /////////////////////////////////////////////
	// utility methods to connect the lexer
	// and the parser.

	private void advance() {
		current = lexer.nextToken();
	}

	private void eatToken(Kind kind) {
		if (kind == current.kind)
			advance();
		else {
			error("Expects: " + kind.toString() + " But got: "
					+ current.kind.toString());
		}
	}

	private void error(String errMsg) {
		System.out.println("Syntax error (at line " + current.lineNum
				+ " column " + current.columNum + " ): " + errMsg);
		return;
	}

	// ////////////////////////////////////////////////////////////
	// below are method for parsing.

	// A bunch of parsing methods to parse expressions. The messy
	// parts are to deal with precedence and associativity.

	// ExpList -> Exp ExpRest*
	// ->
	// ExpRest -> , Exp
	private java.util.LinkedList<ast.exp.T> parseExpList() {
		if (current.kind == Kind.TOKEN_RPAREN)
			return null;
		java.util.LinkedList<ast.exp.T> exps = new java.util.LinkedList<ast.exp.T>();
		exps.add(parseExp());
		while (current.kind == Kind.TOKEN_COMMER) {
			advance();
			exps.add(parseExp());
		}
		return exps;
	}

	// AtomExp -> (exp)
	// -> INTEGER_LITERAL
	// -> true
	// -> false
	// -> this
	// -> id
	// -> new int [exp]
	// -> new id ()
	private ast.exp.T parseAtomExp() {
		ast.exp.T exp;
		String id;
		switch (current.kind) {
		case TOKEN_LPAREN:
			advance();
			exp = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			return exp;
		case TOKEN_NUM:
			int num = Integer.parseInt(current.lexeme);
			advance();
			return new ast.exp.Num(num, current.lineNum);
		case TOKEN_TRUE:
			advance();
			return new ast.exp.True(current.lineNum);
		case TOKEN_FALSE:
			advance();
			return new ast.exp.False(current.lineNum);
		case TOKEN_THIS:
			advance();
			return new ast.exp.This(current.lineNum);
		case TOKEN_ID:
			id = current.lexeme;
			advance();
			return new ast.exp.Id(id, current.lineNum);
		case TOKEN_NEW: {
			advance();
			switch (current.kind) {
			case TOKEN_INT:
				advance();
				eatToken(Kind.TOKEN_LBRACK);
				exp = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.exp.NewIntArray(exp, current.lineNum);
			case TOKEN_ID:
				id = current.lexeme;
				advance();
				eatToken(Kind.TOKEN_LPAREN);
				eatToken(Kind.TOKEN_RPAREN);
				return new ast.exp.NewObject(id, current.lineNum);
			default:
				error("after keyword \"new\" should be followed by \"int\" or object name.");
				return null;
			}
		}
		default:
			error(current.lexeme + " can not be explained in the expression.");
			return null;
		}
	}

	// NotExp -> AtomExp
	// -> AtomExp .id (expList)
	// -> AtomExp [exp]
	// -> AtomExp .length
	private ast.exp.T parseNotExp() {
		ast.exp.T array = parseAtomExp();
		while (current.kind == Kind.TOKEN_DOT
				|| current.kind == Kind.TOKEN_LBRACK) {
			if (current.kind == Kind.TOKEN_DOT) {
				advance();
				if (current.kind == Kind.TOKEN_LENGTH) {
					advance();
					return new ast.exp.Length(array, current.lineNum);
				}
				String id = current.lexeme;
				eatToken(Kind.TOKEN_ID);
				eatToken(Kind.TOKEN_LPAREN);
				java.util.LinkedList<ast.exp.T> exps = parseExpList();
				eatToken(Kind.TOKEN_RPAREN);
				return new ast.exp.Call(array, id, exps, current.lineNum);
			} else {
				advance();
				ast.exp.T index = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.exp.ArraySelect(array, index, current.lineNum);
			}
		}
		return array;
	}

	// TimesExp -> ! TimesExp
	// -> NotExp
	private ast.exp.T parseTimesExp() {
		int count = 0;
		while (current.kind == Kind.TOKEN_NOT) {
			advance();
			count++;
		}
		ast.exp.T exp = parseNotExp();

		for (int i = 0; i < count; i++)
			exp = new ast.exp.Not(exp, current.lineNum);
		return exp;
	}

	// AddSubExp -> TimesExp * TimesExp
	// -> TimesExp
	private ast.exp.T parseAddSubExp() {
		ast.exp.T left = parseTimesExp();
		while (current.kind == Kind.TOKEN_TIMES) {
			advance();
			ast.exp.T right = parseTimesExp();
			left = new ast.exp.Times(left, right, current.lineNum);
		}
		return left;
	}

	// LtExp -> AddSubExp + AddSubExp
	// -> AddSubExp - AddSubExp
	// -> AddSubExp
	private ast.exp.T parseLtExp() {
		ast.exp.T left = parseAddSubExp();
		while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
			Kind kind = current.kind;
			advance();
			ast.exp.T right = parseAddSubExp();
			if (kind == Kind.TOKEN_ADD)
				left = new ast.exp.Add(left, right, current.lineNum);
			else
				left = new ast.exp.Sub(left, right, current.lineNum);
		}
		return left;
	}

	// AndExp -> LtExp < LtExp
	// -> LtExp
	private ast.exp.T parseAndExp() {
		ast.exp.T left = parseLtExp();
		while (current.kind == Kind.TOKEN_LT) {
			advance();
			ast.exp.T right = parseLtExp();
			left = new ast.exp.Lt(left, right, current.lineNum);
		}
		return left;
	}

	// Exp -> AndExp && AndExp
	// -> AndExp
	private ast.exp.T parseExp() {
		ast.exp.T left = parseAndExp();
		while (current.kind == Kind.TOKEN_AND) {
			advance();
			ast.exp.T right = parseAndExp();
			left = new ast.exp.And(left, right, current.lineNum);
		}
		return left;
	}

	// Statement -> { Statement* }
	// -> if ( Exp ) Statement else Statement
	// -> while ( Exp ) Statement
	// -> System.out.println ( Exp ) ;
	// -> id = Exp ;
	// -> id [ Exp ]= Exp ;
	private ast.stm.T parseStatement() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a statement.
		String id;
		ast.exp.T exp;
		int lineNum = current.lineNum;
		if (temp != null) {
			id = temp.lexeme;
			temp = null;
			switch (current.kind) {
			case TOKEN_ASSIGN:
				advance();
				exp = parseExp();
				eatToken(Kind.TOKEN_SEMI);
				return new ast.stm.Assign(id, exp, lineNum);
			case TOKEN_LBRACK:
				id = current.lexeme;
				advance();
				ast.exp.T index = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				eatToken(Kind.TOKEN_ASSIGN);
				exp = parseExp();
				eatToken(Kind.TOKEN_SEMI);
				return new ast.stm.AssignArray(id, index, exp, lineNum);
			default:
				error("expression error. illegal character \"" + current.lexeme
						+ "\" after " + id + ".");
				return null;
			}
		}
		switch (current.kind) {
		case TOKEN_LBRACE:
			eatToken(Kind.TOKEN_LBRACE);
			parseStatements();
			eatToken(Kind.TOKEN_RBRACE);
			return null;
		case TOKEN_IF:
			advance();
			eatToken(Kind.TOKEN_LPAREN);
			ast.exp.T condition = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.stm.Block thenn,elsee;
			
			if(current.kind == Kind.TOKEN_LBRACE)
			{
				eatToken(Kind.TOKEN_LBRACE);
				thenn = new ast.stm.Block(parseStatements());
				eatToken(Kind.TOKEN_RBRACE);
			}
			else
			    thenn = new ast.stm.Block(new util.Flist<ast.stm.T>().addAll(parseStatement()));
			eatToken(Kind.TOKEN_ELSE);
			
			if(current.kind == Kind.TOKEN_LBRACE)
			{
				eatToken(Kind.TOKEN_LBRACE);
				elsee = new ast.stm.Block(parseStatements());
				eatToken(Kind.TOKEN_RBRACE);
			}
			else
			    elsee = new ast.stm.Block(new util.Flist<ast.stm.T>().addAll(parseStatement()));
			return new ast.stm.If(condition, thenn, elsee, lineNum);
		case TOKEN_WHILE:
			advance();
			eatToken(Kind.TOKEN_LPAREN);
			condition = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.stm.Block body;
			if (current.kind == Kind.TOKEN_LBRACE) {
				advance();
				body = new ast.stm.Block(parseStatements());
				eatToken(Kind.TOKEN_RBRACE);
			} else {
				java.util.LinkedList<ast.stm.T> stms = new java.util.LinkedList<ast.stm.T>();
				stms.add(parseStatement());
				body = new ast.stm.Block(stms);
			}
			return new ast.stm.While(condition, body, lineNum);
		case TOKEN_SYSTEM:
			advance();
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_OUT);
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_PRINTLN);
			eatToken(Kind.TOKEN_LPAREN);
			exp = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			eatToken(Kind.TOKEN_SEMI);
			return new ast.stm.Print(exp, lineNum);
		case TOKEN_ID:
			String tempID = current.lexeme;
			advance();
			switch (current.kind) {
			case TOKEN_ASSIGN:
				advance();
				exp = parseExp();
				eatToken(Kind.TOKEN_SEMI);
				return new ast.stm.Assign(tempID, exp, lineNum);
			case TOKEN_LBRACK:
				advance();
				ast.exp.T index = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				eatToken(Kind.TOKEN_ASSIGN);
				exp = parseExp();
				eatToken(Kind.TOKEN_SEMI);
				return new ast.stm.AssignArray(tempID, index, exp, lineNum);
			default:
				error("expression error. illegal character \"" + current.lexeme
						+ "\" after " + tempID + ".");
				return null;
			}
		default:
			error("expression error. illegal character \"" + current.lexeme
					+ "\".");
			return null;
		}
	}

	// Statements -> Statement Statements
	// ->
	private java.util.LinkedList<ast.stm.T> parseStatements() {
		java.util.LinkedList<ast.stm.T> stms = new java.util.LinkedList<ast.stm.T>();
		if(temp != null)
			stms.add(parseStatement());
		while (current.kind == Kind.TOKEN_LBRACE
				|| current.kind == Kind.TOKEN_IF
				|| current.kind == Kind.TOKEN_WHILE
				|| current.kind == Kind.TOKEN_SYSTEM
				|| current.kind == Kind.TOKEN_ID) {
			stms.add(parseStatement());
		}
		return stms;
	}

	// Type -> int []
	// -> boolean
	// -> int
	// -> id
	private ast.type.T parseType() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a type.
		switch (current.kind) {
		case TOKEN_INT:
			advance();
			if (current.kind == Kind.TOKEN_LBRACK) {
				eatToken(Kind.TOKEN_LBRACK);
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.type.IntArray();
			}
			return new ast.type.Int();
		case TOKEN_BOOLEAN:
			advance();
			return new ast.type.Boolean();
		case TOKEN_ID:
			String classId;
			if(temp!=null)
				classId = temp.lexeme;
			else
			{
			    classId = current.lexeme;
			    advance();
			}
			return new ast.type.Class(classId);
		default:
			error("illegal type. Can not recongize " + current.lexeme + ".");
			return null;
		}
	}

	// VarDecl -> Type id ;
	private ast.dec.T parseVarDecl() {
		// to parse the "Type" nonterminal in this method, instead of writing
		// a fresh one.
		ast.type.T type = null;
		String id = null;
		int lineNum = current.lineNum;
		if (current.kind == Kind.TOKEN_ID) {
			temp = current;
			advance();
			if (current.kind == Kind.TOKEN_ID) {
				type = parseType();
				temp = null;
				id = current.lexeme;
				advance();
				eatToken(Kind.TOKEN_SEMI);
				return new ast.dec.Dec(type, id, lineNum);
			}
			return null;
		} else {
			type = parseType();
			id = current.lexeme;
			eatToken(Kind.TOKEN_ID);
			eatToken(Kind.TOKEN_SEMI);
			return new ast.dec.Dec(type, id, lineNum);
		}
	}

	// VarDecls -> VarDecl VarDecls
	// ->
	private java.util.LinkedList<ast.dec.T> parseVarDecls() {
		java.util.LinkedList<ast.dec.T> declsList = new java.util.LinkedList<ast.dec.T>();
		while (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			ast.dec.T dec = parseVarDecl();
			if (temp != null)
			  break;
			declsList.add(dec);	
		}
		return declsList;
	}

	// FormalList -> Type id FormalRest*
	// ->
	// FormalRest -> , Type id
	private java.util.LinkedList<ast.dec.T> parseFormalList() {
		java.util.LinkedList<ast.dec.T> formalList = new java.util.LinkedList<ast.dec.T>();
		ast.type.T type = null;
		String id = null;
		if (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			type = parseType();
			id = current.lexeme;
			int lineNum = current.lineNum;
			formalList.add(new ast.dec.Dec(type, id, lineNum));
			eatToken(Kind.TOKEN_ID);
			while (current.kind == Kind.TOKEN_COMMER) {
				advance();
				type = parseType();
				id = current.lexeme;
				formalList.add(new ast.dec.Dec(type, id, lineNum));
				eatToken(Kind.TOKEN_ID);
			}
		}
		return formalList;
	}

	// Method -> public Type id ( FormalList )
	// { VarDecl* Statement* return Exp ;}
	private ast.method.T parseMethod() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a method.
		String id;
		eatToken(Kind.TOKEN_PUBLIC);
		ast.type.T parType = parseType();
		id = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LPAREN);
		java.util.LinkedList<ast.dec.T> parFormallst = parseFormalList();
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		java.util.LinkedList<ast.dec.T> parseVDecl = parseVarDecls();
		java.util.LinkedList<ast.stm.T> parsestmt = parseStatements();
		eatToken(Kind.TOKEN_RETURN);
		ast.exp.T parExp = parseExp();
		eatToken(Kind.TOKEN_SEMI);
		eatToken(Kind.TOKEN_RBRACE);
		return new ast.method.Method(parType, id, parFormallst, parseVDecl,
				parsestmt, parExp);
	}

	// MethodDecls -> MethodDecl MethodDecls
	// ->
	private java.util.LinkedList<ast.method.T> parseMethodDecls() {
		java.util.LinkedList<ast.method.T> methods = new java.util.LinkedList<ast.method.T>();
		while (current.kind == Kind.TOKEN_PUBLIC) {
			methods.add(parseMethod());
		}
		return methods;
	}

	// ClassDecl -> class id { VarDecl* MethodDecl* }
	// -> class id extends id { VarDecl* MethodDecl* }
	private ast.classs.T parseClassDecl() {
		String id, extendss = null;
		eatToken(Kind.TOKEN_CLASS);
		id = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		if (current.kind == Kind.TOKEN_EXTENDS) {
			eatToken(Kind.TOKEN_EXTENDS);
			extendss = current.lexeme;
			eatToken(Kind.TOKEN_ID);
		}
		eatToken(Kind.TOKEN_LBRACE);
		java.util.LinkedList<ast.dec.T> decls = parseVarDecls();
		java.util.LinkedList<ast.method.T> methods = parseMethodDecls();
		eatToken(Kind.TOKEN_RBRACE);
		return new ast.classs.Class(id, extendss, decls, methods);
	}

	// ClassDecls -> ClassDecl ClassDecls
	// ->
	private java.util.LinkedList<ast.classs.T> parseClassDecls() {
		java.util.LinkedList<ast.classs.T> classes = new java.util.LinkedList<ast.classs.T>();
		while (current.kind == Kind.TOKEN_CLASS) {
			classes.add(parseClassDecl());
		}
		return classes;
	}

	// MainClass -> class id
	// {
	// public static void main ( String [] id )
	// {
	// Statement
	// }
	// }
	private ast.mainClass.T parseMainClass() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a main class as described by the
		// grammar above.
		String id, arg;

		eatToken(Kind.TOKEN_CLASS);
		id = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LBRACE);
		eatToken(Kind.TOKEN_PUBLIC);
		eatToken(Kind.TOKEN_STATIC);
		eatToken(Kind.TOKEN_VOID);
		eatToken(Kind.TOKEN_MAIN);
		eatToken(Kind.TOKEN_LPAREN);
		eatToken(Kind.TOKEN_STRING);
		eatToken(Kind.TOKEN_LBRACK);
		eatToken(Kind.TOKEN_RBRACK);
		arg = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		ast.stm.T stm = parseStatement();
		eatToken(Kind.TOKEN_RBRACE);
		eatToken(Kind.TOKEN_RBRACE);
		return new ast.mainClass.MainClass(id, arg, stm);
	}

	// Program -> MainClass ClassDecl*
	private ast.program.T parseProgram() {
		ast.mainClass.T mainclass = parseMainClass();
		java.util.LinkedList<ast.classs.T> classes = parseClassDecls();
		eatToken(Kind.TOKEN_EOF);
		return new ast.program.Program(mainclass, classes);
	}

	public ast.program.T parse() {
		return parseProgram();
	}
}
