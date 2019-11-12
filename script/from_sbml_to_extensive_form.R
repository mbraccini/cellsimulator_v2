#!/usr/bin/env Rscript
library(BoolNet)
library(tcltk)

args = commandArgs(trailingOnly=TRUE)
if (length(args)!=1) {
  stop("One argument must be supplied (input file).sbml", call.=FALSE)
} 

`%+%` <- function(a,b) paste(paste(a, collapse=""),paste(b, collapse=","), sep=":")
`%+-+%` <- function(a,b) paste(paste(a, collapse=""),paste(b, collapse=""), sep=":")

if (length(args)==0) {
  stop("One argument must be supplied (input file).sbml", call.=FALSE)
} 
print(args[1]) 

fileConn<-file(paste0(gsub(pattern = "\\.sbml$", "", args[1]),".txt")) #nome file di output

n = loadSBML(args[1],symbolic=FALSE)
l <- as.vector(n$genes)
i <- 0
res <- list()

res[[length(res) + 1]] <- "#Id nodo: lista di nodi entranti\n"
res[[length(res) + 1]] <- "Topology:"
for (g in l){
    res[[length(res) + 1]] <- (i %+%  rev(sapply(n$interactions[[g]]$input, FUN=function(x) x-1))) #con rev inverto l'ordine degli input così che nel mio simulatore sia coerente con l'output delle tabelle di verità
    i <- i + 1
}


res[[length(res) + 1]] <- "\n# La seguente descrizione topologica"
res[[length(res) + 1]] <- "# 0: x1, x2"
res[[length(res) + 1]] <- "# con la seguente lista di valori di output (vedi 'Functions E:')"
res[[length(res) + 1]] <- "# 0: 0101"
res[[length(res) + 1]] <- "# corrisponde alla Truth Table del nodo con Id 0:"
res[[length(res) + 1]] <- "# x2 x1 | (t+1)"
res[[length(res) + 1]] <- "# 0  0  | 0"
res[[length(res) + 1]] <- "# 0  1  | 1"
res[[length(res) + 1]] <- "# 1  0  | 0"
res[[length(res) + 1]] <- "# 1  1  | 1\n"

res[[length(res) + 1]] <- "Functions E:"

i <- 0
for (g in l){
    res[[length(res) + 1]] <- (i %+-+%  n$interactions[[g]]$func)
    i <- i + 1
}
res[[length(res) + 1]] <- "\n#Id nodo: nome del gene/proteina...\n"

res[[length(res) + 1]] <- "Names:"
res[[length(res) + 1]] <- paste(seq(0,length(n$genes) - 1,1),n$genes, sep=":")

writeLines (unlist(res), fileConn)
close(fileConn)
