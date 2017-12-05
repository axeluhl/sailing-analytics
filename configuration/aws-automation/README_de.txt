Beschreibung des Skripts:

0. Voraussetzungen für Verwendung
  0.1 AWS CLI konfigurieren
  0.2 Standardwerte anpassen
  0.3 Bedienung anzeigen
1. Aufbau 
  1.1 Dateien und ihre Aufgaben
  1.2 Szenarios
2. Funktionalität
  2.1 Initialisierung von benötigten Variablen
  2.2 Fehlerbehandlung 
  2.3 Meldungsausgabe
3. TODO
  3.1 Fehlerbehebungen
  3.2 Verbesserungen
  3.3 Erweiterungen

0. Voraussetzungen für Verwendung

- AWS CLI (https://aws.amazon.com/de/cli)
- Cygwin Packages:
-- tmux
-- jq
-- openssh
-- wget
-- curl

0.1 AWS CLI konfigurieren

In die Konsole muss "aws configure" eingegeben werden. Anschließend müssen Access Key und Secret Access Key 
deines AWS Benutzers eingegeben werden.

Falls noch nicht vorhanden, kann über die AWS IAM Konsole ein entsprechender Schlüssel generiert werden.
Dazu navigiert ein priviligierter Benutzer in der AWS Webkonsole zu folgendem Eintrag: 
IAM-Konsole >> Users >> dein Benutzername >> Security Credentials >> Create access key

0.2 Standardwerte anpassen

Zur Vermeidung der wiederholten Eingabe von nutzerspezifischen Informationen, können in der Datei
aws_variables.sh bestimmte Standardwerte für Variablen festgelegt werden. Es ist zum Beispiel 
möglich die standardmäßige Region festzulegen, den standardmäßigen Schlüsselname zur Verbindung zu einer
Instanz und weiteres.

0.3 Bedienung anzeigen

Das Skript zeigt über den Aufruf ./aws-setup.sh oder ./aws-setup.sh --help Hinweise zur Bedienung an.

1. Aufbau

1.1 Dateien und ihre Aufgaben

aws-setup.sh:
Funktionen für Parameteraufnahme, Sourcing und Dokumentation. 

lib/functions_app.sh:
Funktionen für die Modifikation des Zustands der SAP Sailing Analytics Instanz (Eventerstellung, Passwortänderung, Apache-Konfiguration)

lib/functions_ec2.sh:
Funktionen für die Erstellung von EC2-Instanzen und Abfrage von deren Attributen

lib/functions_elb.sh:
Funktionen für die Erstellung Load Balancern, Listener, Rules und Target Groups

lib/functions_io.sh:
Funktionen für die Verarbeitung von Nutzereingaben und Initialisieren von Variablen

lib/functions_route53.sh:
Funktionen für die Erstellung von Route53 Einträgen

lib/functions_tmux.sh:
Funktionen für die Ausführung von Befehlen über tmux und zum Aufbau der Benutzeroberfläche

lib/functions_wrapper.sh:
Funktionen für die Fehlerverarbeitung von Funktionen und Vereinfachung von wiederholten Aufrufen

lib/scenario_associate_alb.sh,
lib/scenario_associate_clb.sh,
lib/scenario_associate_elastic_ip.sh,
lib/scenario_instance.sh,
lib/scenarion_tail.sh:
Siehe skriptinterne Dokumentation

lib/util_functions.sh:
Funktionen für die Erleichterung der Benutzereingabe, Validierungsfunktionen und sonstige potenziell relevanten Hilfsfunktionen

lib/util_variables.sh:
Sonstiges Hilfsvariablen (Zeitstempel, Skriptname, etc.)

lib/utils.sh:
Funktionen für die Ausgabe von farbigen Meldungen, Logging und Sourcing der restlichen Bash-Dateien

1.2 Szenarios

Szenarios sind gekapselte Ausführungseinheiten, die durch Orchestrierung von AWS- und instanzspezifischen
Funktionen einen bestimmten Use-Case automatisieren.

Szenarios beinhalten folgende Funktionen:

- Funktion zum Start des Ausführung des Szenarios
- Funktion zur Überprüfung von Vorbedingungen (Abhängigkeiten von Paketen oder Umgebungsvariablen) [optional]
- Funktion zur Sicherstellung der Initialisierung von benötigen Variablen
- Funktion zur Ausführung der Programmlogik

2. Funktionalität

2.1 Initialisierung von benötigten Variablen

Jedes Szenario benötigt für seine Ausführung bestimmte inititalisierte Variablen. Die Zuweisung eines Werts zu einer Variable erfolgt
entweder beim Start des Skripts über die Mitgabe eines Parameters oder falls kein Parameter übergeben worden ist, über die Eingabe des
Benutzers nach entsprechender Aufforderung. Wenn der Benutzer zur Eingabe eines Werts für eine Variable aufgefordert wird, wird automatisch 
ein Standardwert aus der Datei aws_variables.sh vorgeschlagen. Dies erleichtert die Benutzereingabe, da sich bestimmte Werte, wie zum Beispiel der Name
des Keypairs zur Verbindung einer Instanz selten ändern. In der Datei werden außerdem regionspezifische Variablenwerte, wie zum Beispiel
die IDs der Sicherheitsgruppen oder Images festgelegt.

2.2 Fehlerbehandlung

Um die erfolgreiche Ausführung einer Funktion zu überprüfen werden verschiedene Strategien verfolgt:

1. Überprüfung des Rückgabewerts einer Funktion

Diese Strategie wird bei AWS-spezifischen Funktionen angewandt. Bei Aufruf eines AWS-Kommandos (z.B. aws elb create-load-balancer) wird
bei Erfolg der Rückgabewert 0 zurückgegeben und bei Auftritt eines Fehlers der Rückgabewert 1.

2. Überpüfen des HTTP-Codes

Bei Aufruf eines curl-Kommandos wird die Option -w "\n%{http_code}" mitübergeben. Dabei werden Response- und HTTP-Code auf zwei Zeilen
aufgeteilt und damit ermöglicht, dass nach Aufruf der Funktion die Zuordnung zu zwei verschiedenen Variablen erfolgt. Es wird dann verglichen, ob der
HTTP-Statuscode mit dem erwarteten Ergebnis übereinstimmt. 

2.3 Meldungsausgabe

Innerhalb von Methoden wird der Nutzer über Erfolg oder Misserfolg benachrichtigt. Die Ausgabe erfolgt über den
Standardfehlerkanal "stderr".

3. TODO

3.1 Fehlerbehebungen

- Nutzer erstellen scheitert bisher mit "Precondition failed" (Nutzername erfüllt Richtlinie nicht)

3.2 Verbesserungen

- run-instance Funktion nach "aws_functions.sh" faktorisieren
- Anpassung der Dateierzeugung an Linux ("crlf" ersetzen)

3.3 Erweiterungen

- list-Funktion mit Anzeige aller Instanznamen und deren DNS, Instanz-ID, etc. damit --tail nicht die Web-GUI erfordert
- Detailiertere Ausgabe von Zwischenergebnisse des Skripts über die Konsole 
- weitere Szenarios hinzufügen (Multi-Instanz-Setup, Shutdown, Replica Mgmt.)
- regionspezifisches Sourcing von Dateien mit Variablen (Auskommentieren nicht mehr notwendig)



Getestet unter Microsoft Windows [Version 10.0.10586]:
- Cygwin 2.8.2(0.313/5/3)
- aws-cli/1.11.129
- tmux 2.8.2(0.313/5/3)
- openssh 7.5p1-1
- wget 1.19.1
- curl 7.53.0

=> Funktioniert (Route53-Eintrag Erstellung konnte bisher nicht getestet werden)

Getestet unter Microsoft Windows [Version 6.3.96001]:
- Cygwin 2.9.0(0.318/5/3)
- aws-cli/1.11.166
- tmux 2.4
- openssh 7.4p1
- wget 1.19.1
- curl 7.55.1-1

=> Funktioniert (Route53-Eintrag Erstellung konnte bisher nicht getestet werden)
