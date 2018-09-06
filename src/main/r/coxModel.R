# TODO: Add comment
# 
# Author: robchallen
###############################################################################
# https://tbrieder.org/epidata/course_e_ex04_task.pdf
# https://stats.stackexchange.com/questions/256148/stratification-in-cox-model
# http://www.sthda.com/english/wiki/cox-proportional-hazards-model

library(datasets)

library(reshape2)
library(survival)
library(survminer)
library(broom)
library(GGally)
library(randomForest)
library(plotrix)
library(tree)
library(reprtree)
library(sjPlot)

library(dplyr)

options(repos='http://cran.rstudio.org')
have.packages <- installed.packages()
cran.packages <- c('devtools','plotrix','randomForest','tree')
to.install <- setdiff(cran.packages, have.packages[,1])
if(length(to.install)>0) install.packages(to.install)

library(devtools)
if(!('reprtree' %in% installed.packages())){
  install_github('araastat/reprtree')
}
for(p in c(cran.packages, 'reprtree')) eval(substitute(library(pkg), list(pkg=p)))

# source_directory <- getSrcDirectory(function(dummy) {dummy});
# if (source_directory == "") source_directory="/home/robchallen/Git/working-drafts/src/main/r"
# source(paste(source_directory,'/standardAxes.R',sep=""));
# source(paste(source_directory,'/utils.R',sep=""));
# createDirectory();

load(file="~/R/timeToViewData");

###
# Time to view cox model




count(data,"discipline_name")
counts <- lapply( covariates, function(x) {eval(substitute(count(data,tmp),list(tmp=as.name(x))))})
sink("characteristics.txt", append=FALSE, split=FALSE)
lapply(counts,function(x){x})
sink();



# tidy(univ_models$investigation_abnormal,exponentiate = TRUE)
# glance(univ_models$investigation_abnormal)
# plot(survfit(cox), ylim=c(0, 1), xlab="minutes",
# 		ylab="not viewed")




covariates_all <- c(
  "discipline_name", 
  "investigation_abnormal",  
  "dependency_level", 
  "patient_decade",
  "weekday_cat",
  "day_time_cat",
  "emis",
  "patient_id_updated",
  "publish_rate")

multiv_formula = as.formula(paste("Surv(minutes_to_view, viewed) ~ ",paste(covariates_all,collapse=" + ")));
multiv_model <- coxph(multiv_formula, data = data)
write.table(
  tidy(multiv_model,exponentiate = TRUE),
  file="~/Dropbox/ECMM433 data science/cox/multivariate_all.txt", sep="\t");
rm(multiv_model);

covariates_baseline <- c(
  "dependency_level", 
  "patient_decade",
  "days_processing", 
  "weekday_cat",
  "day_time_cat",
  "emis",
  "patient_id_updated",
  "publish_rate")

multiv_formula = as.formula(paste("Surv(minutes_to_view, viewed) ~ ",paste(covariates_baseline,collapse=" + ")));
multiv_model <- coxph(multiv_formula, data = data_baseline)
write.table(
  tidy(multiv_model,exponentiate = TRUE),
  file="~/Dropbox/ECMM433 data science/cox/multivariate_baseline.txt", sep="\t");
rm(multiv_model);


univariate <- function(var, input) {
  univ_formula = as.formula(paste0("Surv(minutes_to_view, viewed) ~ ",var));
  univ_model <- coxph(univ_formula, data = input) # %>% sample_n(200000))
  tmp <- tidy(univ_model,exponentiate = TRUE)
  rm(univ_model)
  #sink(paste0("~/Dropbox/ECMM433 data science/cox/univariate_",var,".txt"))
  #print(summary(univ_model))
    # print(
    #   xtable(x),type="html", html.table.attributes=""
    # )
  # collect <- rbind.all.columns(collect,tidy(univ_model,exponentiate = TRUE))
  #sink()
  return(tmp)#rm(univ_model);
}
# var <- "investigation_name"
models <- lapply(covariates_baseline,univariate,input=data_baseline)
univ <- do.call("rbind", models)
write.table(univ, file="~/Dropbox/ECMM433 data science/cox/univariate_baseline.txt",sep="\t")

models <- lapply(covariates_all,univariate,input=data)
univ <- do.call("rbind", models)
write.table(univ, file="~/Dropbox/ECMM433 data science/cox/univariate_all.txt",sep="\t")

model <- univariate("dependency_level", data_baseline)

# multiv_model2 <- coxph(Surv(minutes_to_view, viewed) ~ day_time_cat + investigation_abnormal + patient_group + emis +patient_id_updated, data = data)
# sink("multivariate2.txt", append=FALSE, split=FALSE);
# summary(multiv_model2);
# tidy(multiv_model2,exponentiate = TRUE);
# sink();
# 
# 
# multiv_model_abnorm <- coxph(Surv(minutes_to_view, viewed) ~ day_time_cat + patient_group + emis +patient_id_updated, data = data_baseline)
# sink("multivariateAbnorm.txt", append=FALSE, split=FALSE);
# summary(multiv_model_abnorm);
# tidy(multiv_model_abnorm,exponentiate = TRUE);
# sink();

covariates <- c(
  "dependency_level", 
  "patient_decade",
  "days_processing", 
  "weekday_cat",
  "day_time_cat",
  "emis",
  "patient_id_updated",
  "publish_rate")

data_baseline <- data_baseline %>% mutate( 
  viewed_TF = as.factor(ifelse(viewed ==1, TRUE, FALSE)),
  dependency_level = as.factor(dependency_level),
  patient_id_updated = ifelse(patient_id_updated ==1, TRUE, FALSE)
)

tmp_baseline <- union(
	data_baseline %>% filter(viewed ==1) %>% sample_n(5000),
	data_baseline %>% filter(viewed ==0) %>% sample_n(5000),
) %>% select(one_of(c("viewed_TF",covariates)))

tmp_baseline_10000 <- 
	data_baseline %>% sample_n(10000) %>% select(one_of(c("viewed_TF",covariates)))

rm(data)
rm(data_baseline)

rf_formula = as.formula(paste("viewed_TF ~ ",paste(covariates,collapse=" + ")));

# TODO: sampling rather than classwt
# https://stats.stackexchange.com/questions/157714/r-package-for-weighted-random-forest-classwt-option/

rf <- randomForest(formula =rf_formula, data=tmp_baseline_10000, 
	importance=TRUE,proximity=TRUE, na.action=na.omit, ntree=200,
    classwt=c("FALSE"=1.2, "TRUE"=1), nodesize=100, maxnodes=50
)



sink("~/Dropbox/ECMM433 data science/cox/random_forest_imbalanced.txt", append=FALSE, split=FALSE);
print(rf)
importance(rf)
sink();

rf <- randomForest(rf_formula, data=tmp_baseline, importance=TRUE,proximity=TRUE, na.action=na.omit,
				 ntree=200, nodesize=100, maxnodes=50
                   )

sink("~/Dropbox/ECMM433 data science/cox/random_forest_balanced.txt", append=FALSE, split=FALSE);
# reprtree:::plot.getTree(rf)
print(rf)
importance(rf)
# varImpPlot(rf)
sink();

count(data_baseline$viewed)
