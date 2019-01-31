library(tidyverse)
library(reshape2)
library(cowplot)
library(lubridate)

files <- list.files(path = "~/Dropbox/litReview/output/", pattern = "*.tsv", all.files = TRUE)
files <- colsplit(files,"\\.",names=c("base","etn"))

# load variables based on content of directory
for (file in files$base) {
  name <- paste0("~/Dropbox/litReview/output/",file,".tsv")
  assign(file, read.delim(name, stringsAsFactors = FALSE))
}

plotArticlesByJournal_Count <- ggplot(getArticlesByJournal %>% top_n(10,totalPagerank))+
  geom_bar(aes(x=reorder(journal,-totalPagerank),y=articles, fill=journal), colour="black", stat="identity")+
  xlab("journal")+ylab("articles")+scale_fill_brewer(palette = "Set3")+
  theme(axis.text.x = element_text(angle = 45, hjust = 1))+
  guides(fill=FALSE)

plotArticlesByPagerank_Pagerank <- ggplot(getArticlesByPagerank)+
  geom_density(aes(x=pagerank))+
  xlab("page rank")

plotArticlesByPagerank_Date <- ggplot(getArticlesByPagerank %>%
    filter(!is.na(date)) %>%
    group_by(month=floor_date(as.Date(date), "year")) %>%
    summarize(amount=n()))+
  geom_col(aes(x=month,y=amount),stat="identity")+
  xlab("year")+ylab("articles")

plotArticlesByPagerank_DateCitedBy <- ggplot(getArticlesByPagerank %>%
    filter(citedByCount!="NULL") ,
    aes(x=as.Date(date),y=as.numeric(citedByCount)))+
  geom_point()+
  geom_smooth(method='lm',formula=y~x)+
  xlab("date")+ylab("count of citing articles")+scale_y_log10()
#Could fit linear model to log of citedByCount, and determine residuals

  
plotArticlesByPagerank_DatePagerank <- ggplot(getArticlesByPagerank, aes(x=as.Date(date),y=pagerank))+
  geom_point()+
  xlab("date")+ylab("pagerank")+ 
  geom_smooth(method='lm')
  