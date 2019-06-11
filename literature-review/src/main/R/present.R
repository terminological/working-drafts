library(tidyverse)
library(reshape2)
library(cowplot)
library(lubridate)
library(huxtable)

# sudo apt-get install libcairo2-dev libmagick++-dev
# install.packages("tidyverse")
# install.packages("cowplot")
# install.packages("lubridate")
# install.packages("huxtable")
# install.packages("flextable")


files <- list.files(path = "~/Dropbox/litReview/output/", pattern = "*.tsv", all.files = TRUE)
files <- colsplit(files,"\\.",names=c("base","etn"))

theme_set(theme_cowplot(font_size=8))

# load variables based on content of directory
for (file in files$base) {
  name <- paste0("~/Dropbox/litReview/output/",file,".tsv")
  assign(file, read.delim(name, stringsAsFactors = FALSE, row.names=NULL))
}

plotArticlesByJournal_Count <- ggplot(getArticlesByJournal %>% top_n(10,totalPagerank)
                %>% mutate(journal = ifelse(journal == "Journal of the American Medical Informatics Association","JAMIA", 
                           journal)))+
  geom_bar(aes(x=reorder(journal,-totalPagerank),y=articles, fill=journal), colour="black", stat="identity")+
  xlab("journal")+ylab("articles")+scale_fill_brewer(palette = "Set3")+
  theme(axis.text.x = element_text(angle = 90, hjust = 1))+
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
  geom_point(size=1)+
  geom_smooth(method='lm',formula=y~x)+
  xlab("date")+ylab("count of citing articles")+scale_y_log10()+
  coord_cartesian(xlim = as.Date(c('2005-01-01', '2019-01-01')))
plotArticlesByPagerank_DateCitedBy

figure1pg = plot_grid(plotArticlesByJournal_Count, 
          plot_grid(plotArticlesByPagerank_Date, plotArticlesByPagerank_DateCitedBy, labels=c("B","C"), ncol=1),
          labels=c("A",NA), ncol=2)
save_plot("~/Dropbox/litReview/output/figure1.png", figure1pg, base_width = 7)
save_plot("~/Dropbox/litReview/output/figure1.svg", figure1pg, base_width = 7)

# plotArticlesByPagerank_DateDomainCitedBy <- ggplot(getArticlesByPagerank,
#   aes(x=as.Date(date),y=domainCitedByCount))+
#   geom_point()+
#   geom_smooth(method='lm',formula=y~x)+
#   xlab("date")+ylab("count of citing domain articles")+scale_y_log10()+
#   coord_cartesian(xlim = as.Date(c('2005-01-01', '2019-01-01')))
# plotArticlesByPagerank_DateDomainCitedBy
#Could fit linear model to log of citedByCount, and determine residuals

# plotArticlesByPagerank_DatePagerank <- ggplot(
#   getArticlesByPagerank, 
#   aes(x=as.factor(year(as.Date(date))),y=pagerank))+
#   geom_violin(na.rm = TRUE, scale = "count")+
#   xlab("date")+ylab("pagerank")+
#   # geom_smooth(method='lm')+
#   coord_cartesian(
# #    xlim = as.factor(year(as.Date(c('2005-01-01', '2019-01-01')))),
#     ylim = c(0.15, 0.175)
#     )
# plotArticlesByPagerank_DatePagerank

###########################################

defaultLayout = function(hux) {
  return( hux %>% 
    set_bold(1, everywhere, TRUE) %>% 
    set_top_border(1, everywhere, 1) %>%
    set_bottom_border(1, everywhere, 2) %>%
    set_bottom_border(nrow(hux), everywhere, 1) %>%
    set_width("400pt") %>%
    set_wrap(TRUE) %>%
    set_all_padding(everywhere,everywhere,2) %>%
    set_valign(everywhere,everywhere,"top") )
}



mergeCells = function(hux) {
  tmpHux = hux
  for (tt in unique(hux[[1]][-1])) {
    tmp = seq(1,length(hux[[1]][-1])+1)
    l = min(tmp[hux[[1]] == tt])
    r = max(tmp[hux[[1]] == tt])
    tmpHux <- merge_cells(tmpHux, l:r, 1)
    tmpHux <- tmpHux %>% set_top_border(l, everywhere, 1)
  }
  return(tmpHux)
}

##############################################

top10articles<-getArticlesByPagerank %>% top_n(10, pagerank) %>%
  select(reference = node,pagerank) %>%
  mutate(reference = sub("\\[[0-9]+\\]","",reference))
htTop10Articles <- defaultLayout(as_huxtable(top10articles, add_colnames = TRUE)) %>%
  set_align(1, 2, 'right') %>%
  set_col_width(c(.8, .2)) %>%
  set_caption('Top 10 articles by page rank')
quick_html(htTop10Articles, file="~/Dropbox/litReview/output/top10Refs.html")
# quick_docx(ht, file="~/Dropbox/litReview/output/top10Refs.docx")

###############################################

top10CitedArticles<-getArticlesByPagerank %>%
  mutate(citedByCount = as.numeric(citedByCount)) %>% top_n(10, citedByCount) %>%
  arrange(desc(citedByCount)) %>%
  select(reference = node,citedByCount) %>%
  mutate(reference = sub("\\[[0-9]+\\]","",reference))
htTop10CitedArticles <- defaultLayout(as_huxtable(top10CitedArticles, add_colnames = TRUE)) %>%
  set_align(1, 2, 'right') %>%
  set_col_width(c(.8, .2)) %>%
  set_number_format(everywhere,2,0) %>%
  set_caption('Top 10 articles by citation count')
quick_html(htTop10CitedArticles, file="~/Dropbox/litReview/output/top10CitedRefs.html")
# quick_docx(ht, file="~/Dropbox/litReview/output/top10Refs.docx")

###############################################

top5ByTopic <- getTopicDocuments %>% left_join(getArticlesByPagerank, by="nodeId") %>% group_by(topic) %>%
  top_n(5,weight) %>%
  mutate(reference = sub("\\[[0-9]+\\]","",node)) %>% 
  select(topic,reference,weight) 

htTop5ByTopic <- defaultLayout(as_huxtable(top5ByTopic, add_colnames = TRUE)) %>% 
  set_align(1, 3, 'right') %>%
  set_align(everywhere, 1, 'left') %>%
  set_width("400pt") %>%
  set_col_width(c(.1, .7, .2)) %>%
  set_caption('Top 5 articles by topic')
htTop5ByTopic = mergeCells(htTop5ByTopic)
quick_html(htTop5ByTopic, file="~/Dropbox/litReview/output/top5RefsByTopic.html")

##################################################


top5ByAuthorCommunity <- getAuthorCommunityArticles %>%
  mutate(authorCommunity=community) %>%
  inner_join(getAuthorCommunityLabels, by="authorCommunity") %>%
  mutate(nodeId=articleId, community=label) %>%
  left_join(getArticlesByPagerank, by="nodeId") %>% group_by(community) %>%
  mutate(pagerank = pagerank.x) %>%
  top_n(5,pagerank) %>%
  filter(row_number() < 6) %>%
  arrange(desc(pagerank), .by_group = TRUE) %>%
  mutate(reference = sub("\\[[0-9]+\\]","",node)) %>% 
  select(community,reference,pagerank) 

htTop5ByAuthorCommunity <- defaultLayout(as_huxtable(top5ByAuthorCommunity, add_colnames = TRUE)) %>% 
  set_align(1, 3, 'right') %>%
  set_align(everywhere, 1, 'left') %>%
  set_width("400pt") %>%
  set_col_width(c(.1, .7, .2)) %>%
  set_caption('Top 5 articles by author community')
htTop5ByAuthorCommunity <- mergeCells(htTop5ByAuthorCommunity)

quick_html(htTop5ByAuthorCommunity, file="~/Dropbox/litReview/output/top5RefsByAuthorCommunity.html")

##################################################


top5ByArticleGroup <- getArticlesByPagerank %>%
  mutate(articleGroup=articleCommunity) %>%
  inner_join(getArticleGroupLabels, by="articleGroup") %>%
  mutate(articleGroup = label) %>%
  group_by(articleGroup) %>%
  top_n(5,pagerank) %>%
  filter(row_number() < 6) %>%
  arrange(desc(pagerank), .by_group = TRUE) %>%
  mutate(reference = sub("\\[[0-9]+\\]","",node)) %>% 
  select(articleGroup,reference,pagerank) 

htTop5ByArticleGroup <- defaultLayout(as_huxtable(top5ByArticleGroup, add_colnames = TRUE)) %>% 
  set_align(1, 3, 'right') %>%
  set_align(everywhere, 1, 'left') %>%
  set_width("400pt") %>%
  set_col_width(c(.1, .7, .2)) %>%
  set_caption('Top 5 articles by article group')
htTop5ByArticleGroup <- mergeCells(htTop5ByArticleGroup)

quick_html(htTop5ByArticleGroup, file="~/Dropbox/litReview/output/top5RefsByArticleGroup.html")

##############################################

authorCommunityByMember <- getAuthorCommunityLabels %>% 
  mutate(community = authorCommunity) %>% 
  left_join(getAuthorCoauthorHarmonicCentrality) %>%
  mutate(name = paste0(lastName,", ",firstName)) %>%
  # mutate(affiliation = str_trunc(affiliation,50,"right")) %>%
  mutate(affiliation = str_extract(affiliation, "([^,]+,[^,]+)|([^,]*)")) %>%
  group_by(label) %>% top_n(5,pagerank) %>%
  arrange(desc(pagerank), .by_group = TRUE) %>%
  select(community = label, name, affiliation, pagerank)

htAuthorsByCommunity <- defaultLayout(as_huxtable(authorCommunityByMember, add_colnames = TRUE)) %>% 
  set_align(1, 4, 'right') %>%
  set_col_width(c(.1, .3, .5, .1)) %>%
  set_caption('Top 5 pageranked researchers in community')
htAuthorsByCommunity = mergeCells(htAuthorsByCommunity)

quick_html(htAuthorsByCommunity, file="~/Dropbox/litReview/output/top5AuthorsByCommunity.html")

##########################################################
# confusion matrices ----

topicArticleGroupXmap = getTopicArticleCommunity %>% mutate(articleGroup = articleCommunity) %>% left_join(getArticleGroupLabels) %>%
  select(topic,articleGroup=label,totalScore)

topicArticleConfusion <- ggplot(topicArticleGroupXmap, aes(x=as.factor(topic), y=articleGroup, fill=totalScore)) +
  geom_tile(colour = "white") +
  scale_fill_gradient(low = "white",high = "steelblue", position="bottom") +
  xlab("topic") + ylab("article group") + theme(legend.position="none")



topicAuthorCommunityXmap = getTopicCommunity %>% mutate(authorCommunity = community) %>% left_join(getAuthorCommunityLabels) %>%
  select(topic,authorCommunity = label,totalScore)

topicAuthorConfusion <- ggplot(topicAuthorCommunityXmap, aes(x=as.factor(topic), y=authorCommunity, fill=totalScore)) +
  geom_tile(colour = "white") +
  scale_fill_gradient(low = "white",high = "green", position="bottom") +
  xlab("topic") + ylab("author community") + theme(legend.position="none")


articleGroupAuthorCommunityXmap = getAuthorCommunityArticleGroup %>% 
  mutate(articleGroup = articleCommunity) %>% 
  left_join(getArticleGroupLabels) %>%
  mutate(articleGroup = label) %>% select(-label) %>%
  left_join(getAuthorCommunityLabels) %>%
  select(articleGroup,authorCommunity = label,totalScore)

articleAuthorConfusion <- ggplot(articleGroupAuthorCommunityXmap , aes(x=articleGroup, y=authorCommunity, fill=totalScore)) +
  geom_tile(colour = "white") +
  scale_fill_gradient(low = "white",high = "orange", position="bottom") +
  xlab("article group") + ylab("author community") + theme(legend.position="none")

table(articleGroupAuthorCommunityXmap)

csq1 = chisq.test(xtabs(totalScore ~ articleGroup + authorCommunity, articleGroupAuthorCommunityXmap), correct=TRUE)
csq2 = chisq.test(xtabs(totalScore ~ as.factor(topic) + articleGroup, topicArticleGroupXmap), correct=TRUE)
csq3 = chisq.test(xtabs(totalScore ~ as.factor(topic) + authorCommunity, topicAuthorCommunityXmap), correct=TRUE)

conv = function(tmp, name) {return(tibble(
  "Comparison"=name,
  "Chi-squared"=unname(tmp$statistic),
  "Degrees"=unname(tmp$parameter),
  "P value"=tmp$p.value
))}

chiSquaredTests = defaultLayout(huxtable(
  rbind(
    conv(csq1,"Article group vs Author community"),
    conv(csq2,"NLP topic vs Article group"),
    conv(csq3,"NLP topic versus Author community")
  ),add_colnames = TRUE
))
quick_html(chiSquaredTests, file="~/Dropbox/litReview/output/chiSquaredTests.html")


figure2pg = plot_grid(
  topicArticleConfusion,
  topicAuthorConfusion,
  articleAuthorConfusion,
  labels=c("A","B","C"), ncol=3)
save_plot("~/Dropbox/litReview/output/figure2.png", figure2pg, base_width = 7, base_height = 2)
save_plot("~/Dropbox/litReview/output/figure2.svg", figure2pg, base_width = 7, base_height = 2)

############################################################

tmp <- getTestSetReferences %>% mutate(referenceDoi = str_to_lower(referenceDoi)) %>%
  left_join(getArticlesByPagerank %>% mutate(referenceDoi = doi), c("referenceDoi")) %>%
  mutate(id=referenceDoi) %>% left_join(getTestSetReferencesDetails, c("id"))

tmp2 <- tmp %>% group_by(doi.x) %>% summarize(id = head(doi.x,1),
  count = n(), matched = sum(if_else(is.na(title),0,1))) %>% 
  mutate(frac = matched/count) %>% 
  arrange(desc(frac)) %>% select(-doi.x)

tmp3 <- tmp2 %>% left_join(getTestSetDetails)

# group by articles with high numbers of references - are these a closer match to topics?
# are there some hot spots?
