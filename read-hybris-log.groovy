def sout = new StringBuilder()
def serr = new StringBuilder()
def proc = 'tail -f ../../../../log/hybris-server.log'.execute()
proc.consumeProcessOutput(sout, serr)
proc.waitForOrKill(1000)
println "out> $sout err> $serr"
