library(DBI)
library(odbc)
library(datasets)
library(reshape2)
library(dplyr)
library(ggplot2)

# devtools::install_github("briatte/ggnet")
library(sna)
library(network)
library(ggnet)

library(circlize)

source_directory <- getSrcDirectory(function(dummy) {dummy});
if (source_directory == "") source_directory="/home/robchallen/Git/working-drafts/src/main/r"
source(paste(source_directory,'/standardAxes.R',sep=""));
source(paste(source_directory,'/utils.R',sep=""));
createDirectory();

pwd <- readline(prompt="Enter DB: ");
con <- dbConnect(odbc(),
                 Driver = "ODBC Driver 13 for SQL Server",
                 Server = "10.174.129.118",
                 Database = "RobsDatabase",
                 UID = "RobertSQL",
                 PWD = pwd,
                 Port = 1433);

rs <- dbSendQuery(con, "SELECT * FROM percent_results_updated_by_date;")
tmp <- dbFetch(rs)
dbClearResult(rs)

cols <- c("all"="#0000ff","tsft"="#ff00ff")
ggplot(tmp %>% filter(date>'2015-10-08'), aes(x=date))+
  geom_point(aes(y=percent_updated,colour='all'), size=0.5)+
  geom_smooth(aes(y=percent_updated,colour='all'),method='lm')+
  geom_point(aes(y=tsft_percent_updated,colour='tsft'), size=0.5)+
  geom_smooth(aes(y=tsft_percent_updated,colour='tsft'),method='lm')+
  scale_color_manual(name="test origin",values=cols)+
  ylab('percent updated identifiers')
ggsave('identifiersUpdateRate.png',width=10,height=5,units='in')

dbListTables(con);

nodes <- dbReadTable(con, "tmpIdNode");
edges <- dbReadTable(con, "tmpIdEdge");
graphSize  <- dbReadTable(con, "tmpIdGraph");

# <- nodes %>% group_by(graph_id) %>% dplyr::summarize(
#   identifiers=n(),
#   emis=sum(ifelse(type=="E",1,0)),
#   nhs=sum(ifelse(type=="N",1,0)),
#   mrn=sum(ifelse(type=="M",1,0))
# #  rba=sum(ifelse(type=="M" && starts_with("MRBA",vars="node_id"),1,0))
# );

ggplot(graphSize)+
  geom_bar(aes(identifiers), fill="#c0c0c0", colour="#000000", width=0.8)+
  scale_y_log10(labels = scales::comma)+
  ggtitle("Log plot of freqency of identifiers");
ggsave('identifiersFreqAll.png',width=10,height=5,units='in')

ggplot(graphSize)+
  geom_bar(aes(emis), fill="#ffc0c0", colour="#000000", width=0.8)+
  scale_y_log10(labels = scales::comma)+
  ggtitle("Log plot of freqency of internal identifiers");
ggsave('identifiersFreqEmis.png',width=10,height=5,units='in')

ggplot(graphSize)+
  geom_bar(aes(nhsnos), fill="#c0ffc0", colour="#000000", width=0.8)+
  scale_y_log10(labels = scales::comma)+
  ggtitle("Log plot of freqency of nhs numbers");
ggsave('identifiersFreqNhs.png',width=10,height=5,units='in')

ggplot(graphSize)+
  geom_bar(aes(mrns), fill="#c0c0ff", colour="#000000", width=0.8)+
  scale_y_log10(labels = scales::comma)+
  ggtitle("Log plot of freqency of mrns");
ggsave('identifiersFreqMrn.png',width=10,height=5,units='in')

exampleSmallGraph <- graphSize %>% filter(identifiers == 5) %>% top_n(5,graph_id)
tmpGraph <- edges %>% inner_join(exampleSmallGraph, by="graph_id") %>% select(source_node_id,target_node_id,graph_id);

net <- network(
  tmpGraph,
  matrix.type = "edgelist"
)

ggnet2(net, size = 0) +
  geom_label(aes(fill = substring(label, 1, 1), label = gsub("(....).+(\\S\\S)\\s*","\\1XXX\\2",label)
  ))+
  scale_color_brewer() + guides(fill=FALSE)
ggsave('smallGraphExample.png',width=10,height=10,units='in')

# Add in the results from the request / result graph
for (graph_id in exampleSmallGraph$graph_id) {
  rs <- dbSendQuery(con, paste0("SELECT source_node_id,target_node_id,graph_id FROM tmpRequestResultGraph WHERE graph_id=",graph_id,";"));
  # dbFetch(rs)
  tmpGraph <- tmpGraph  %>% union(dbFetch(rs));
  dbClearResult(rs);
}

net <- network(
  tmpGraph,
  matrix.type = "edgelist"
)

ggnet2(net, size = 0) +
  geom_label(aes(fill = substring(label, 1, 1), label = gsub("(....).+(\\S\\S)\\s*","\\1XXX\\2",label)
                 ))+
  scale_color_brewer() + guides(fill=FALSE)
ggsave('smallGraphExampleWithResults.png',width=20,height=20,units='in')

# LOOK at a single example fo a larger graph
exampleLargeGraph <- graphSize %>%
  # filter(identifiers > 20) %>%
  filter(nhs > 1) %>%
  filter(mrn > 3) %>%
  filter(emis > 10) %>%
  top_n(1,graph_id)

tmpGraph <- edges %>% inner_join(exampleLargeGraph, by="graph_id") %>% select(source_node_id,target_node_id,graph_id);

net2 <- network(
  tmpGraph,
  matrix.type = "edgelist"
)

ggnet2(net2, mode = "fruchtermanreingold", layout.par = list(cell.jitter = 0.75), size = 0) +
  geom_label(
    aes(
      fill = substring(label, 1, 1), 
      label = gsub("(....).+(\\S\\S\\s*)","\\1XXX\\2",label) # toupper(substring(trimws(label), 2))
    ))+
  scale_color_brewer() + guides(fill=FALSE)
ggsave('largeGraphExample.png',width=10,height=10,units='in')

# get result and request relationships for larger graph
for (graph_id in exampleLargeGraph$graph_id) {
  rs <- dbSendQuery(con, paste0("SELECT source_node_id,target_node_id,graph_id FROM tmpRequestResultGraph WHERE graph_id=",graph_id,";"));
  # dbFetch(rs)
  tmpGraph <- tmpGraph  %>% union(dbFetch(rs));
  dbClearResult(rs);
}

net2 <- network(
  tmpGraph,
  matrix.type = "edgelist"
)

ggnet2(net2, mode = "fruchtermanreingold", layout.par = list(cell.jitter = 0.75), size = 0) +
  geom_label(
    aes(
      fill = substring(label, 1, 1), 
      label = gsub("(....).+(\\S\\S\\s*)","\\1XXX\\2",label) # toupper(substring(trimws(label), 2))
      ))+
  scale_color_brewer() + guides(fill=FALSE)
ggsave('largeGraphExampleWithResults.png',width=20,height=20,units='in')

# https://jokergoo.github.io/circlize_book/book/

cat <- function(number,type) {
  return(
    paste0(type,
    cut(number,breaks=c(-Inf,0.5,1.5,2.5,3.5,Inf),labels=c("0","1","2","3","4"),include.lowest=TRUE))
  )
}

# https://jokergoo.github.io/circlize_book/book/advanced-usage-of-chorddiagram.html#customize-sector-labels

summaryGraphNE <- graphSize %>% group_by(from=cat(emis,"E"),to=cat(nhs,"N")) %>% dplyr::summarise( value=n(), type="E" ) %>% arrange(from,desc(to))
summaryGraphMN <- graphSize %>% group_by(from=cat(nhs,"N"),to=cat(mrn,"M")) %>% dplyr::summarise( value=n(), type="N" ) %>% arrange(from,desc(to))
summaryGraphEM <- graphSize %>% group_by(from=cat(mrn,"M"),to=cat(emis,"E")) %>% dplyr::summarise( value=n(), type="M" ) %>% arrange(from,desc(to))

# segments <- summaryGraph %>% group_by(type) %>% dplyr::summarize(count = n_distinct(from))
grid.col = c(N0 = "#6060ff", N1 = "#8080ff", N2 = "#A0A0ff",
             E1 = "#ff8080", E2 = "#ffa0a0", E3 = "#ffc0c0", E4 = "#ffe0e0",
             M0 = "#60ff60", M1 = "#80ff80", M2 = "#A0ffA0", M3 = "#C0ffC0", M4 = "#E0ffE0")



layout(matrix(1:3, 1, 3))
circos.clear()

circos.clear()
circos.par(gap.after = c(rep(2,4),15,rep(2,3),15), start.degree = -7+90)
chordDiagram(summaryGraphEM ,directional = 0,grid.col = grid.col,
             col= colorRamp2(c(min(summaryGraphEM$value),max(summaryGraphEM$value)), c("#808080","#C0C0C0"), transparency = 0.5),
             annotationTrack = c("grid"),
             preAllocateTracks = list(track.height = max(strwidth(summaryGraphEM$from),strwidth(summaryGraphEM$to))))
circos.track(track.index = 1, panel.fun = function(x, y) {
  circos.text(CELL_META$xcenter, CELL_META$ylim[1], CELL_META$sector.index, 
              facing = "clockwise", niceFacing = TRUE, adj = c(0, 0.5))
}, bg.border = NA)           
abline(v = 0, lty = 2, col = "#00000080")

circos.clear()
circos.par(gap.after = c(rep(2,2),15,rep(2,4),15), start.degree = -8+90)
chordDiagram(summaryGraphMN ,directional = 0,grid.col = grid.col,
             col= colorRamp2(c(min(summaryGraphMN$value),max(summaryGraphMN$value)), c("#808080","#C0C0C0"), transparency = 0.5),
             annotationTrack = c("grid"),
             preAllocateTracks = list(track.height = max(strwidth(summaryGraphMN$from),strwidth(summaryGraphMN$to))))
# we go back to the first track and customize sector labels
circos.track(track.index = 1, panel.fun = function(x, y) {
  circos.text(CELL_META$xcenter, CELL_META$ylim[1], CELL_META$sector.index, 
              facing = "clockwise", niceFacing = TRUE, adj = c(0, 0.5))
}, bg.border = NA)
abline(v = 0, lty = 2, col = "#00000080")

circos.par(gap.after = c(rep(2,3),15,rep(2,2),15), start.degree = -8+90)
chordDiagram(summaryGraphNE ,directional = 0,grid.col = grid.col,
             col= colorRamp2(c(min(summaryGraphNE$value),max(summaryGraphNE$value)), c("#808080","#C0C0C0"), transparency = 0.5),
             annotationTrack = c("grid"),
             preAllocateTracks = list(track.height = max(strwidth(summaryGraphMN$from),strwidth(summaryGraphMN$to))))
circos.track(track.index = 1, panel.fun = function(x, y) {
  circos.text(CELL_META$xcenter, CELL_META$ylim[1], CELL_META$sector.index, 
              facing = "clockwise", niceFacing = TRUE, adj = c(0, 0.5))
}, bg.border = NA)
abline(v = 0, lty = 2, col = "#00000080")


dev.copy2pdf(file = "chordDiagram.pdf", height=3, width=10)

