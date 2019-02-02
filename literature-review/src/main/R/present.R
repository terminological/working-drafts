library(tidyverse)
library(reshape2)
library(cowplot)
library(lubridate)
library(huxtable)

# sudo apt-get install libcairo2-dev libmagick++-dev
# install.packages(huxtable)
# install.packages(flextable)


files <- list.files(path = "~/Dropbox/litReview/output/", pattern = "*.tsv", all.files = TRUE)
files <- colsplit(files,"\\.",names=c("base","etn"))

# load variables based on content of directory
for (file in files$base) {
  name <- paste0("~/Dropbox/litReview/output/",file,".tsv")
  assign(file, read.delim(name, stringsAsFactors = FALSE, row.names=NULL))
}

plotArticlesByJournal_Count <- ggplot(getArticlesByJournal %>% top_n(10,totalPagerank))+
  geom_bar(aes(x=reorder(journal,-totalPagerank),y=articles, fill=journal), colour="black", stat="identity")+
  xlab("journal")+ylab("articles")+scale_fill_brewer(palette = "Set3")+
  theme(axis.text.x = element_text(angle = 45, hjust = 1))+
  guides(fill=FALSE)
plotArticlesByJournal_Count

# plotArticlesByPagerank_Pagerank <- ggplot(getArticlesByPagerank)+
#   geom_density(aes(x=pagerank))+
#   xlab("page rank")
# plotArticlesByPagerank_Pagerank

plotArticlesByPagerank_Date <- ggplot(getArticlesByPagerank %>%
    filter(!is.na(date)) %>%
    group_by(month=floor_date(as.Date(date), "year")) %>%
    summarize(amount=n()))+
  geom_col(aes(x=month,y=amount),stat="identity")+
  xlab("year")+ylab("articles")
plotArticlesByPagerank_Date

plotArticlesByPagerank_DateCitedBy <- ggplot(getArticlesByPagerank %>%
    filter(citedByCount!="NULL") ,
    aes(x=as.Date(date),y=as.numeric(citedByCount)))+
  geom_point()+
  geom_smooth(method='lm',formula=y~x)+
  xlab("date")+ylab("count of citing articles")+scale_y_log10()+
  coord_cartesian(xlim = as.Date(c('2005-01-01', '2019-01-01')))
plotArticlesByPagerank_DateCitedBy

# plotArticlesByPagerank_DateDomainCitedBy <- ggplot(getArticlesByPagerank,
#   aes(x=as.Date(date),y=domainCitedByCount))+
#   geom_point()+
#   geom_smooth(method='lm',formula=y~x)+
#   xlab("date")+ylab("count of citing domain articles")+scale_y_log10()+
#   coord_cartesian(xlim = as.Date(c('2005-01-01', '2019-01-01')))
# plotArticlesByPagerank_DateDomainCitedBy
#Could fit linear model to log of citedByCount, and determine residuals

plotArticlesByPagerank_DatePagerank <- ggplot(getArticlesByPagerank, aes(x=as.Date(date),y=pagerank))+
  geom_point()+
  xlab("date")+ylab("pagerank")+ 
  geom_smooth(method='lm')+
  coord_cartesian(xlim = as.Date(c('2005-01-01', '2019-01-01')))
plotArticlesByPagerank_DatePagerank

top10articles<-getArticlesByPagerank %>% top_n(10, pagerank) %>%
  select(reference = node,pagerank) %>%
  mutate(reference = sub("\\[[0-9]+\\]","",reference))
htTop10Articles <- as_huxtable(top10articles, add_colnames = TRUE) %>%
  set_bold(1, 1:2, TRUE) %>% 
  set_top_border(1, 1:2, 2) %>%
  set_bottom_border(1, 1:2, 2) %>%
  set_bottom_border(nrow(top10articles)+1, 1:2, 1) %>%
  set_align(1, 2, 'right') %>%
  set_width("400pt") %>%
  set_wrap(TRUE) %>%
  set_col_width(c(.8, .2)) %>%
  set_caption('Top 10 articles by page rank')
quick_html(htTop10Articles, file="~/Dropbox/litReview/output/top10Refs.html")
# quick_docx(ht, file="~/Dropbox/litReview/output/top10Refs.docx")

top5ByTopic <- getTopicDocuments %>% left_join(getArticlesByPagerank, by="nodeId") %>% group_by(topic) %>%
  top_n(5,weight) %>%
  mutate(reference = sub("\\[[0-9]+\\]","",node)) %>% 
  select(topic,reference,weight) 

htTop5ByTopic <- as_huxtable(top5ByTopic, add_colnames = TRUE) %>% 
  set_bold(1, 1:3, TRUE) %>% 
  set_top_border(1, 1:3, 2) %>%
  set_bottom_border(1, 1:3, 2) %>%
  set_bottom_border(nrow(top5ByTopic)+1, 1:3, 1) %>%
  set_align(1, 3, 'right') %>%
  set_align(1:nrow(top5ByTopic)+1, 1, 'left') %>%
  set_width("400pt") %>%
  set_wrap(TRUE) %>%
  set_col_width(c(.1, .7, .2)) %>%
  set_caption('Top 5 articles by topic')

for (tt in (top5ByTopic %>% distinct(topic))$topic ) {
  tmp = seq(2,nrow(top5ByTopic)+1)
  l = min(tmp[top5ByTopic$topic == tt])
  r = max(tmp[top5ByTopic$topic == tt])
  htTop5ByTopic <- merge_cells(htTop5ByTopic, l:r, 1)
  htTop5ByTopic <- htTop5ByTopic %>% set_top_border(l, 1:3, 1)
}

quick_html(htTop5ByTopic, file="~/Dropbox/litReview/output/top5RefsByTopic.html")

authorCommunityByMember <- getAuthorCommunityLabels %>% 
  mutate(community = authorCommunity) %>% 
  left_join(getAuthorCoauthorHarmonicCentrality) %>%
  mutate(name = paste0(lastName,", ",firstName)) %>%
  mutate(affiliation = str_trunc(affiliation,50,"right")) %>%
  group_by(label) %>% top_n(5,pagerank) %>%
  arrange(desc(pagerank), .by_group = TRUE) %>%
  select(community = label, name, affiliation, pagerank)

htAuthorsByCommunity <- as_huxtable(authorCommunityByMember, add_colnames = TRUE) %>% 
  set_bold(1, 1:4, TRUE) %>% 
  set_top_border(1, 1:4, 2) %>%
  set_bottom_border(1, 1:4, 2) %>%
  set_bottom_border(nrow(top5ByTopic)+1, 1:4, 1) %>%
  set_align(1, 4, 'right') %>%
  set_width("400pt") %>%
  set_wrap(TRUE) %>%
  set_col_width(c(.1, .3, .5, .1)) %>%
  set_caption('Top 5 pageranked researchers in community')
  
for (tt in getAuthorCommunityLabels$label ) {
  tmp = seq(2,nrow(authorCommunityByMember)+1)
  l = min(tmp[authorCommunityByMember$community == tt])
  r = max(tmp[authorCommunityByMember$community == tt])
  htAuthorsByCommunity <- merge_cells(htAuthorsByCommunity, l:r, 1)
  htAuthorsByCommunity <- htAuthorsByCommunity %>% set_top_border(l, 1:4, 1)
}

quick_html(htAuthorsByCommunity, file="~/Dropbox/litReview/output/top5AuthorsByCommunity.html")

# confusion matrices

topicArticleGroupXmap = getTopicArticleCommunity %>% mutate(articleGroup = articleCommunity) %>% left_join(getArticleGroupLabels) %>%
  select(topic,articleGroup=label,totalScore)

ggplot(topicArticleGroupXmap, aes(x=as.factor(topic), y=articleGroup, fill=totalScore)) +
  geom_tile(colour = "white") +
  scale_fill_gradient(low = "white",high = "steelblue") +
  xlab("topic") + ylab("article group")

topicAuthorCommunityXmap = getTopicCommunity %>% mutate(authorCommunity = community) %>% left_join(getAuthorCommunityLabels) %>%
  select(topic,authorCommunity = label,totalScore)

ggplot(topicAuthorCommunityXmap, aes(x=as.factor(topic), y=authorCommunity, fill=totalScore)) +
  geom_tile(colour = "white") +
  scale_fill_gradient(low = "white",high = "green") +
  xlab("topic") + ylab("author community")

articleGroupAuthorCommunityXmap = getAuthorCommunityArticleGroup %>% 
  mutate(articleGroup = articleCommunity) %>% 
  left_join(getArticleGroupLabels) %>%
  mutate(articleGroup = label) %>% select(-label) %>%
  left_join(getAuthorCommunityLabels) %>%
  select(articleGroup,authorCommunity = label,totalScore)

ggplot(articleGroupAuthorCommunityXmap , aes(x=articleGroup, y=authorCommunity, fill=totalScore)) +
  geom_tile(colour = "white") +
  scale_fill_gradient(low = "white",high = "orange") +
  xlab("topic") + ylab("author community")
