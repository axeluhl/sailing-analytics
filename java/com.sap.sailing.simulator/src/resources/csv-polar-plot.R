
library("plotrix")

mode.png <- TRUE

##p.data <- read.table("p49-generated.csv", sep=";", dec=".", header=TRUE)
p.data <- read.table("c:/temp/pd-test-49stg.csv", sep=";", dec=".", header=TRUE)

my.colors <- function (n, alpha = 1) {
  if ((n <- as.integer(n[1L])) > 0) {
    k <- n%/%2
    h <- c(4/12, 2/12, 0/12)
    s <- c(1, 1, 1)
    v <- c(0.5, 0.9, 0.75)
    c(hsv(h = seq.int(h[1L], h[2L], length.out = k),
          s = seq.int(s[1L], s[2L], length.out = k),
          v = seq.int(v[1L], v[2L], length.out = k), alpha = alpha),
      hsv(h = seq.int(h[2L], h[3L], length.out = n - k + 1)[-1L],
          s = seq.int(s[2L], s[3L], length.out = n - k + 1)[-1L],
          v = seq.int(v[2L], v[3L], length.out = n - k + 1)[-1L], alpha = alpha))
  }
  else character()
}

allcols <- my.colors(ncol(p.data)-1,alpha=1)

if (mode.png) {
  png(paste("polar-49er-stg-simulator-",gsub("X","",paste(colnames(p.data)[2:ncol(p.data)],collapse="-")),"-kn.png",sep=""),res=300,height=1500,width=1500)
}

##for(wcol in 2:ncol(p.data)) {
for(wcol in ncol(p.data):2) {

  p.tmp<-subset(p.data[,c(1,wcol)],!is.na(p.data[,wcol]))

  for(idx in nrow(p.tmp):1) {
    tmp=c(360-p.tmp[idx,1],p.tmp[idx,2])
    p.tmp<-rbind(p.tmp,tmp)
  }

  if (wcol==ncol(p.data)) {
    add <- FALSE
  } else {
    add <- TRUE
  }
  
  if (!add) {
    polar.plot(p.tmp[,2],p.tmp[,1],radial.lim=c(0,15),start=90,clockwise=TRUE,rp.type="p",lwd=2,cex=1,point.symbols=16,line.col=allcols[wcol-1], point.col=allcols[wcol-1],add=add)
  }
  polar.plot(p.tmp[,2],p.tmp[,1],radial.lim=c(0,15),start=90,clockwise=TRUE,rp.type="p",lwd=2,cex=1,point.symbols=16,line.col=allcols[wcol-1], point.col=allcols[wcol-1],show.grid=FALSE,show.grid.labels=FALSE,show.radial.grid=FALSE,add=TRUE)

}

if (mode.png) {
  dev.off()
}
