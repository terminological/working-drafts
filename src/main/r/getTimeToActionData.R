library(DBI)
library(odbc)
library(datasets)
library(dplyr)
library(reshape2)

source_directory <- getSrcDirectory(function(dummy) {dummy});
if (source_directory == "") source_directory="/home/robchallen/Git/working-drafts/src/main/r"
source(paste(source_directory,'/standardAxes.r',sep=""));
source(paste(source_directory,'/utils.r',sep=""));
createDirectory();

con <- dbConnect(odbc(),
                 Driver = "ODBC Driver 13 for SQL Server",
                 Server = "10.174.129.118",
                 Database = "RobsDatabase",
                 UID = "RobertSQL",
                 PWD = rstudioapi::askForPassword("Database password"),
                 Port = 1433);

dbListTables(con);

data3 <- dbReadTable(con, "aggTimeToAction");


cor(x=data3$minutes_to_view, y=data3$minutes_to_action, use="complete.obs")
# cor(x=data2$patient_age, y=data2$minutes_to_resolution, use="complete.obs")
# cor(x=data2$patient_age, y=data2$minutes_to_resolution, use="complete.obs")

timeToActionByTimeToView <- ggplot(
  data=data3 %>% filter(chi_2>80) %>% filter(minutes_to_view > 0) %>% filter(minutes_to_view < 4*60) %>% filter( minutes_to_action < 4*60), 
  aes(x=minutes_to_view, y=minutes_to_action)) +
  geom_bin2d(na.rm = TRUE)

timeToActionByTimeToView
ggsave('timeToActionByTimeToView.png',width=10,height=5,units='in')


# cor(data_no_na$patient_age, data_no_na$minutes_to_resolution)
# cor(data2, use="complete.obs", method="kendall") 

# https://stats.stackexchange.com/questions/108007/correlations-with-categorical-variables


# ggplot(data2,aes(x=minutes_to_resolution,y=minutes_to_view)) + 
#   geom_point(colour="blue", alpha=0.2) + 
#   geom_density2d(colour="black")