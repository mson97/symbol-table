import java.util.HashSet;
import java.util.Stack;

import static com.company.Parser.Scope;

public class SymbolTable {

    private Stack<HashSet<String>> table; // symbol table is stack: scope of hashsets: variables
    private int curr_scope;

    //create stack with empty hash set
    public SymbolTable() {
        table = new Stack<>();
        table.push(new HashSet<>());
        curr_scope = 0;
    }

    // remove last scope from symbol table
    public void exit_scope() {
        table.pop();
        curr_scope--;
    }

    // create new scope in symbol table
    public void enter_scope() {
        table.push(new HashSet<>());
        curr_scope++;
    }

    // checking if variable already exists in scope and adding to symbol table
    public void declare(Token id) {
        if (table.elementAt(curr_scope).contains(id.string)) {
            System.err.println("redeclaration of variable " + id.string);
            // ignore redeclaration
        } else {
            table.elementAt(curr_scope).add(id.string);
        }
    }

    // recursively checking symbol table for variable (inside out)
    public void assign(Token id) {
        if (!find(id)) {
            symbol_error(id);
        }
    }

    // check symbol table for variable when (possibly) given scope
    public boolean assign(Token id, Scope type, int scope) {
        //no scope was given, check recursively
        if (type == Scope.NONE) {
            if (!find(id)) {
                symbol_error(id);
                return false;
            }
        }
        // scope was given, check according to type of scope
        else {
            return locate(id, type, scope);
        }
        return false;
    }

    private boolean locate(Token id, Scope type, int scope) {
        // search at: global lvl
        if (type == Scope.GLOBAL) {
            if (table.elementAt(0).contains(id.string)) {
                return true;
            } else {
                symbol_error(id, type, scope);
            }
        } // search at: current lvl
        else if (type == Scope.CURRENT) {
            if (table.elementAt(curr_scope).contains(id.string)) {
                return true;
            } else {
                symbol_error(id, type, scope);
            }
        } // search at: given (numerical) lvl
        else {
            // scope lvl: invalid, or variable not in scope
            if ((curr_scope - scope < 0) ||
                    !table.elementAt(curr_scope - scope).contains(id.string)) {
                symbol_error(id, type, scope);
            } else {
                return true;
            }
        }
        return false;
    }

    private void symbol_error(Token id) {
        System.err.println(id.string + " is an undeclared variable on line " + id.lineNumber);
        System.exit(1);
    }

    private void symbol_error(Token id, Scope type, int scope) {
        if (type == Scope.GLOBAL) {
            System.err.println("no such variable ~" + id.string + " on line " + id.lineNumber);
        } else {
            System.err.println("no such variable ~" + scope + id.string + " on line " + id.lineNumber);
        }
        System.exit(1);
    }

    private boolean find(Token id) {
        return _find(id, curr_scope);
    }

    private boolean _find(Token id, int scope) {
        if (table.elementAt(scope).contains(id.string)) {
            return true;
        } else if (scope <= 0) {
            return false;
        } else {
            --scope;
            return _find(id, scope);
        }
    }
}
