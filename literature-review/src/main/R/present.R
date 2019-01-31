files <- list.files(path = "~/Dropbox/litReview/output/", pattern = "*.tsv", all.files = TRUE)
for (file in files) {
  data$file <- read.delim(paste0("~/Dropbox/litReview/output/",file), stringsAsFactors = FALSE)
}