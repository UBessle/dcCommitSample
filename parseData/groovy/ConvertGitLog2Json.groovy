import de.iteratec.dcch.GitLogParser

def input = args.size() > 0 ? new File(args[0]) : System.in
def output = args.size() > 1 ? new File(args[1]) : new PrintWriter(System.out)
GitLogParser parser = new GitLogParser(writer:output)
parser.parseText(input)

