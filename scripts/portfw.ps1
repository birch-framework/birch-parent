<#
 = Copyright (c) 2021 Birch Framework
 = This program is free software: you can redistribute it and/or modify
 = it under the terms of the GNU General Public License as published by
 = the Free Software Foundation, either version 3 of the License, or
 = any later version.
 = This program is distributed in the hope that it will be useful,
 = but WITHOUT ANY WARRANTY; without even the implied warranty of
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 = GNU General Public License for more details.
 = You should have received a copy of the GNU General Public License
 = along with this program.  If not, see <https://www.gnu.org/licenses/>.
#>
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