#!/bin/bash

#
# retrieves the commit history from git
#
# usage: retrieveCommitHistory.sh [<GitRepoDir>]
#

currentDir=`pwd`
gitRepoDir=$1

cd $gitRepoDir 
git log --numstat --no-merges

cd $currentDir