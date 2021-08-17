param (
   [Parameter(Mandatory=$false)][string]$action
)

#All the ports you want to forward
$ports = [Array]::AsReadOnly(@(2181,9092,8000,8161,61616,1414,9443,7222,7441,9999))

function Get-WSLPrimaryIP {
   $wslIP = Invoke-Expression "wsl -- hostname -I | wsl -- cut -d' ' -f1"
   return $wslIP
}

function Set-PortFW([string]$RemoteIP, [string[]]$Ports) {
    if ($RemoteIP) {
        $Ports.ForEach({
            Invoke-Expression "netsh interface portproxy add v4tov4 listenport=$_ connectaddress=$RemoteIP connectport=$_"
        })
    }
}

function Clear-PortFW([string[]]$Ports) {
    if ($Ports) {
        $Ports.ForEach({
            Invoke-Expression "netsh interface portproxy delete v4tov4 listenport=$_"
        })
    }
}

function Show-PortFW {
    Invoke-Expression "netsh interface portproxy dump"
}

function Save-DockerHost([string]$RemoteIP) {
    if ($RemoteIP) {
        $RemoteIP = $RemoteIP.Trim()
        $hostsPath = "$env:windir\System32\drivers\etc\hosts"
        $hosts = Get-Content $hostsPath
        $hosts = $hosts | ForEach-Object {
            if ($_ -notmatch '^.*docker\.host\.internal.*$') {
                $_
            }
        }
        $hosts += "$RemoteIP`t`tdocker.host.internal"
        $hosts | Out-File $hostsPath -enc ascii
    }
}

switch ($action) {
    "set" {
        $remoteIP = Get-WSLPrimaryIP
        "Docker remote IP: $remoteIP"
        Set-PortFW -Ports $PORTS -RemoteIP $remoteIP
        Save-DockerHost -RemoteIP $remoteIP
        continue
    }
    "clear" {
        Clear-PortFW -Ports $ports
        continue
    }
    "status" {
        Show-PortFW
        $remoteIP = Get-WSLPrimaryIP
        "Docker remote IP: $remoteIP`n"
        continue
    }
    default {
        "`nUsage:`n"
        "`t" + $MyInvocation.MyCommand.Name + " <status|set|clear>`n"
    }
}