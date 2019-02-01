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
ht <- as_huxtable(top10articles, add_colnames = TRUE) %>%
  set_bold(1, 1:2, TRUE) %>% 
  set_bottom_border(1, 1:2, 1) %>%
  set_align(1, 2, 'right') %>%
  set_width("400pt") %>%
  set_wrap(TRUE) %>%
  set_col_width(c(.7, .3)) %>%
  # set_align(1:4, 2, '.') %>%
  set_caption('Top 10 articles by page rank')
quick_html(ht, file="~/Dropbox/litReview/output/top10Refs.html")
# quick_html(ht)
quick_docx(ht, file="~/Dropbox/litReview/output/top10Refs.docx")
