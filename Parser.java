/* *** This file is given as part of the programming assignment. *** */

public class Parser {

    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok
    private Token tok; // the current token
    private SymbolTable symbolTable;
    private Token variable; // temp variable for symbols
    // private int scope;

    private void scan() {
        tok = scanner.scan();
    }

    private Scan scanner;

    Parser(Scan scanner) {
        this.scanner = scanner;
        symbolTable = new SymbolTable(); // initialize empty symbol table
        scan();
        program();
        if (tok.kind != TK.EOF)
            parse_error("junk after logical end of program");
        symbolTable = null;
    }

    private void program() {
        block();
    }

    private void block() {
        declaration_list();
        statement_list();
    }

    private void declaration_list() {
        // below checks whether tok is in first set of declaration.
        // here, that's easy since there's only one token kind in the set.
        // in other places, though, there might be more.
        // so, you might want to write a general function to handle that.
        while (is(TK.DECLARE)) {
            declaration();
        }
    }

    private void declaration() {
        mustbe(TK.DECLARE);
        // save copy of curr tok
        variable = this.tok;
        mustbe(TK.ID);
        // add variable to symbol table
        symbolTable.declare(variable);
        while (is(TK.COMMA)) {
            scan();
            // save copy
            variable = this.tok;
            mustbe(TK.ID);
            // add variable to symbol table
            symbolTable.declare(variable);
        }
    }

    // check tok is in FS_(statement) = {'~', id, '<', '!', '['}
    private boolean f_stmt() {
        return (((is(TK.TILDA) || is(TK.ID)) || (is(TK.DO) || is(TK.PRINT))) || is(TK.IF));
    }

    private void statement_list() {
        while (f_stmt()) {
            statement();
        }
    }

    private void statement() {
        if (f_asmt()) {
            assignment();
        } else if (is(TK.PRINT)) {
            print();
        } else if (is(TK.DO)) {
            e_do();
        } else if (is(TK.IF)) {
            e_if();
        } else { // incorrect token type
            parse_error("Error in statement");
        }
    }

    // check tok is in FS_(assignment) = {'~', id}
    private boolean f_asmt() {
        return (is(TK.TILDA) || is(TK.ID));
    }

    private void assignment() {
        ref_id();
        // check if variable is in symbol table
        symbolTable.assign(variable);
        mustbe(TK.ASSIGN);
        expr();
    }

    private void print() {
        mustbe(TK.PRINT);
        expr();
    }

    private void e_do() {
        mustbe(TK.DO);
        guarded_command();
        mustbe(TK.ENDDO);
    }

    private void e_if() {
        mustbe(TK.IF);
        guarded_command();
        while (is(TK.ELSEIF)) {
            scan();
            guarded_command();
        }
        if (is(TK.ELSE)) {
            scan();
            // start of nested block, enter new scope
            symbolTable.enter_scope();
            block();
            // end of nested block, exit new scope
            symbolTable.exit_scope();
        }
        mustbe(TK.ENDIF);
    }



    private void ref_id() {
        int scope = -1;
        // Assume Scope: none
        Scope type = Scope.NONE;
        if (is(TK.TILDA)) {
            scan();
            if (is(TK.NUM)) {
                // Scope: given lvl
                type = Scope.GIVEN;
                scope = Integer.parseInt(tok.string);
                scan();
            } else type = Scope.GLOBAL; // Scope: global lvl
        }
        variable = this.tok;
        mustbe(TK.ID);
        // check variable is in symbol table at scope (if given)
        symbolTable.assign(variable, type, scope);
    }

    private void expr() {
        term();
        while (f_addop()) {
            scan();
            term();
        }
    }

    private void guarded_command() {
        expr();
        mustbe(TK.THEN);
        // start of nested block, enter new scope
        symbolTable.enter_scope();
        block();
        // end of nested block, exit new scope
        symbolTable.exit_scope();
    }

    private void term() {
        factor();
        while (f_multop()) {
            scan();
            factor();
        }
    }

    // check tok is in FS_(factor) = {'(', '~', id, number}
    private void factor() {
        if (is(TK.LPAREN)) {
            scan();
            expr();
            mustbe(TK.RPAREN);
        } else if (f_rfid()) {
            ref_id();
        } else if (is(TK.NUM)) {
            scan();
        } else {
            parse_error("Error in factor");
        }
    }

    // check tok is in FS_(ref_id) = {'~', id}
    private boolean f_rfid() {
        return (is(TK.TILDA) || is(TK.ID));
    }

    // check tok is in FS_(addop) = {'+', '-'}
    private boolean f_addop() {
        return (is(TK.PLUS) || is(TK.MINUS));
    }

    // check tok is in FS_(multop) = {'*', '/'}
    private boolean f_multop() {
        return (is(TK.TIMES) || is(TK.DIVIDE));
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
        if (tok.kind != tk) {
            System.err.println("mustbe: want " + tk + ", got " +
                    tok);
            parse_error("missing token (mustbe)");
        }
        scan();
    }

    private void parse_error(String msg) {
        System.err.println("can't parse: line "
                + tok.lineNumber + " " + msg);
        System.exit(1);
    }
}
