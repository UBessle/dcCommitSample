package de.iteratec.dcch

import groovy.json.StreamingJsonBuilder

public class GitLogParser {
    String status = "start"
    Writer writer
    StreamingJsonBuilder jsonBuilder
    Map commitEntry = [:]

    public void parseText(def input) {
        jsonBuilder = new StreamingJsonBuilder(writer)
        writeJsonOutputStart()
        resetParsedValues()
        input.eachLine() { line ->
            switch(line) {
                case ~/^commit .*/:
                    commitEntry.commit = parseCommitHash(line)
                    status = "afterCommit"
                    break
                case ~/^Author:.*/:
                    commitEntry.author = parseAuthor(line)
                    status = "afterAuthor"
                    break
                case ~/^Date:.*/:
                    commitEntry.date = parseDate(line)
                    status = "afterDate"
                    break
                case ~/^\d+\s+\d+.*/:
                    commitEntry.fileList.add(parseFileEntry(line))
                    status = "afterFileEntry"
                    break;
                case ~/^$/:
                    switch (status) {
                        case "afterCommentLine": 
                            commitEntry.commentLineList.add(line)
                            break;
                        case "afterFileEntry":
                            writeParsedCommit()
                            resetParsedValues()
                            break
                        default:
                            break
                    } 
                    break;
                default:
                    commitEntry.commentLineList.add(line)
                    status = "afterCommentLine"
                    break
            }
        }
        writeParsedCommit()    
        resetParsedValues()   
        writeJsonOutputEnd()             
    }
     
    void writeJsonOutputStart() {
        println "["

    }

    void writeJsonOutputEnd() {
        println "]"

    }

    void resetParsedValues() {
        commitEntry = [:]
        commitEntry.commit = ""
        commitEntry.author = ""
        commitEntry.date = ""
        commitEntry.commentLineList = []
        commitEntry.fileList = []
    }
     
    void writeParsedCommit2() {
        String message = commentLineList.join("\n")
        println """    {
        "commit":"${commit}", 
        "author":"${author}", 
        "date":"${date}", 
        "message":"${message}", 
        "files":["""
    fileList.each() {fileEntry ->
        println """            {"addedLines":${fileEntry.addedLines}, "removedLines":${fileEntry.removedLines}, "fileName":"${fileEntry.fileName}"},"""
    }
    println """        ]
    }, """
    }

    void writeParsedCommit() {
        commitEntry.message = commitEntry.commentLineList.join("\n")
        commitEntry.remove("commentLineList")
        jsonBuilder commitEntry
        writer.flush()
        println ","
    }
     
    String parseCommitHash(String line) {
        def parseResult = (line =~ /^commit (\S+)/)
        return parseResult[0][1]
    }
     
    String parseAuthor(String line) {
        def parseResult = (line =~ /^Author: (.*)/)
        return parseResult[0][1]
    }
     
    String parseDate(String line) {
        def parseResult = (line =~ /^Date:\s+\S+ (.*)/)
        Date parseResultDate = Date.parse("MMM dd HH:mm:ss yyyy Z",parseResult[0][1].trim())
        return parseResultDate
    }
     
    Map parseFileEntry(String line) {
        def parseResult = (line =~ /^(\d+)\s+(\d+)\s+(.*)/)
        return ["addedLines":parseResult[0][1], "removedLines":parseResult[0][2], "fileName":parseResult[0][3]]
         
    }
}