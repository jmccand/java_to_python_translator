import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Translator {

    private static String[] twoCharExpressions = {"==", "+=", "-=", "*=", "/=", "<=", ">=", "||", "&&", "++", "--"};

    private File ogJava;
    private String newPython;
    private File pythonFile;

    public Translator(String input, String output) {
	this.ogJava = new File(input);
	this.pythonFile = new File(output);
	this.translate();
    }

    public File getJava() {
	return this.ogJava;
    }

    private void translate() {
	Scanner input;
	try {
	    input = new Scanner(ogJava);
	    List<String> lexed = new ArrayList<String>();
	    while (input.hasNextLine()) {
		String line = input.nextLine();
		List<String> lexedLine = lexer(line);
		for (String token : lexedLine) {
		    lexed.add(token);
		}
		lexed.add("\n");
	    }
	    lexed = clean(lexed);
	    Node program = new Node("program", lexed);
	    write(lexed);
	}
	catch (FileNotFoundException ex) {
	    System.out.println("Cannot find " + ogJava + ", terminating the program");
	    System.exit(1);
	}
    }

    private static List lexer(String line) {
	List<String> lexed = new ArrayList<String>();
	int lineIndex = 0;
	while(lineIndex < line.length()) {
	    String next = lexNext(line, lineIndex);
	    lexed.add(next);
	    lineIndex += next.length();
	    //System.out.println("line index: " + lineIndex + "\n");
	}
	return lexed;
    }

    private static String lexNext(String line, int start) {
	int index = start + 1;
	String total = line.substring(start, start + 1);
	boolean evaluation = eval(total);
	//System.out.println("total: \"" + total + "\" - evaluation: " + evaluation);
	while(evaluation && index < line.length()) {
	    total += line.substring(index, index + 1);
	    index++;
	    evaluation = eval(total);
	    //System.out.println("total: \"" + total + "\" - evaluation: " + evaluation);
	}
	if ((!evaluation) && total.length() - 1 > 0) {
	    total = total.substring(0, total.length() - 1);
	}
	return total;
    }

    private static boolean eval(String total) {
	for (int charIndex = 0; charIndex < total.length(); charIndex++) {
	    switch(total.substring(charIndex, charIndex + 1)) {
	    case " ":
	    case ";":
	    case ")":
	    case "(":
	    case "\"":
	    case "=":
	    case ">":
	    case "<":
	    case "{":
	    case "}":
	    case ":":
	    case "+":
	    case "-":
	    case "*":
	    case "/":
		return false;
	    }
	}
	return true;
    }

    private static List<String> clean(List<String> original) {
	String larger = "";
	List<String> cleaned = new ArrayList<String>();
	int index = 0;
	while (index < original.size() - 1) {
	    if ((original.get(index) + original.get(index + 1)).equals("/*")) {
		larger += "/*";
		index += 2;
		while (!(original.get(index) + original.get(index + 1)).equals("*/")) {
		    larger += original.get(index);
		    index++;
		}
		larger += "*/";
		index += 2;
	    }
	    else if ((original.get(index) + original.get(index + 1)).equals("//")) {
		larger += "//";
		index += 2;
		while (!(original.get(index).equals("\n"))) {
		    larger += original.get(index);
		    index++;
		}
	    }
	    else if (original.get(index).equals("\"")) {
		larger += "\"";
		index ++;
		while (!(original.get(index)).equals("\"")) {
		    larger += original.get(index);
		    index++;
		}
		larger += "\"";
		index ++;
	    }
	    else if (in(twoCharExpressions, original.get(index) + original.get(index + 1))) {
		larger += (original.get(index) + original.get(index + 1));
		index += 2;
	    }
	    else if (original.get(index).equals("-")) {
		larger += "-" + original.get(index + 1);
		index += 2;
	    }
	    else {
		larger = original.get(index);
		index++;
	    }
	    if (!larger.equals(" ")) {
		cleaned.add(larger);
		larger = "";
	    }
	}
	if (index < original.size()) {
	    cleaned.add(original.get(index));
	}
	return cleaned;
    }

    private static boolean in(String[] full, String value) {
	for (String thisValue : full) {
	    if (thisValue.equals(value)) {
		return true;
	    }
	}
	return false;
    }

    private static List<String> toPython(List<String> original) {
	List<String> translated = new ArrayList<String>();
	for (int index = 0; index < original.size(); index++) {
	    String token = original.get(index);
	    String[] smarts = smartTranslate(token);
	    String pythoned = smarts[1];
      
	    translated.add(pythoned);
	}
	return translated;
    }

    private static String[] smartTranslate(String original) {
	switch (original) {
	case "System.out.println":
	    return new String[] {"0", "print"};
	case "private":
	case "public":
	case "static":
	    return new String[] {"0", ""};
	default:
	    return new String[] {"0", original};
	}
    }

    private void write(List<String> source) {
	PrintWriter output;
	try {
	    output = new PrintWriter(pythonFile);
	    String newPython = "";
	    for (String token : source) {
		newPython += token;
		//System.out.print(token + ",");
	    }
	    output.print(newPython);
	    output.close();
	}
	catch (FileNotFoundException ex) {
	    System.out.println("Cannot find " + pythonFile + ", terminating the program");
	    System.exit(1);
	}
    }
}
