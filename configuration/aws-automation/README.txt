Beschreibung des Skripts:

0. Voraussetzungen f�r Nutzung des Skripts

- AWS CLI (https://aws.amazon.com/de/cli)
- Cygwin Packages:
-- tmux
-- jq
-- openssh
-- wget
-- curl

0.1 AWS CLI konfigurieren

"aws configure" durchf�hren.
Falls noch kein Keypair vorhanden:

IAM-Konsole >> Users >> dein Benutzername >> Security Credentials >> Create access key

0.2 Default-Werte anpassen

Default-Werte in "aws_variables.sh" anpassen (key_name, key_file), damit diese nicht
immer neu eingegeben werden m�ssen. Testweise erst einmal in Region "eu-west-2" bleiben.

key_name: Name des Keypairs zur Verbindung zur Instanz
key_file: Pfad zur Datei
...

0.3 Skript ausprobieren

./aws-setup.sh --help
./aws-setup instance-with-load-balancer

Falls Fehler auftaucht:
Skript mit "-d"-Parameter starten und in den Logs der Instanz nachsehen
(./aws-setup.sh --tail --public-dns-name ec2-x...amazon.com)

1. Aufbau:

1.1 Dateien und ihre Hauptaufgaben:

aws-setup-sh:
Parameteraufnahme, Main-Funktion, Dokumentation der Bedienung, Sourcing von lib/utils.sh

lib/aws-functions.sh:
Funktionen f�r AWS (z.B. Load Balancer erstellen) und App-Instanz (z.B. Event erstellen)

lib/aws-variables.sh:
Festlegung von regionspezifischen Konstanten (z.B Hosted Zone ID) und Defaultwerten (z.B Region)

lib/scenario_instance.sh:
Erstellung einer Instanz und anschlie�endes Hinzuf�gen zu einem Load Balancer der �ber einen Route53 Eintrag
mit einer bestimmten Domain (z.B. wcs17.sapsailing.com)  verkn�pft wird.

lib/scenario_tail.sh:
Automatische Anzeige der Log-Dateien einer Instanz mithilfe von tmux

lib/tmux_functions.sh:
Oberfl�chenkonstruktion, Vorbedingungstest, Befehlsausf�hrung

lib/util_functions.sh:
Erleichterung der Benutzereingabe, Validierungsfunktionen, sonstige potenziell relevante Hilfsfunktionen

lib/util_variables.sh:
Sonstiges Hilfsvariablen (Zeitstempel, Skriptname, etc.)

lib/utils.sh:
Ausgabe von farbigen Meldungen, Logging (unbenutzt), Sourcing der restlichen Bash-Dateien im Verzeichnis

1.2 Szenarios

Szenarios sind gekapselte Ausf�hrungseinheiten, die durch Orchestrierung von AWS-Funktionen oder Bereitstellung
eigener Funktionalit�t einen Mehrwert f�r die Automatisierung darstellen.

Szenarios beinhalten folgende Funktionen:

- Funktion zum Start des Ausf�hrung des Szenarios
- Funktion zur �berpr�fung von Vorbedingungen (Abh�ngigkeiten von Paketen oder Umgebungsvariablen) [optional]
- Funktion zur Sicherstellung der Initialisierung von ben�tigen Variablen
- Funktion zur Ausf�hrung der Programmlogik

2. Funktionalit�t

2.1 Initialisierung von ben�tigten Variablen

Jedes Szenario ben�tigt f�r seine Ausf�hrung bestimmte inititalisierte Variablen. Die Zuweisung eines Werts zu einer Variable erfolgt
entweder beim Start des Skripts �ber die Mitgabe eines Parameters oder falls kein Parameter �bergeben worden ist, �ber die Eingabe des
Benutzers nach entsprechender Aufforderung.
Wenn der Benutzer zur Eingabe eines Werts f�r eine Variable aufgefordert wird, wird automatisch ein Default-Wert aus der
Datei "aws_variables.sh" vorgeschlagen. Dies erleichtert die Benutzereingabe, da sich bestimmte Werte, wie z.B. der Name
des Keypairs zur Verbindung einer Instanz selten �ndern. In der Datei werden au�erdem regionspezifische Variablenwerte
(z.B. IDs der Sicherheitsgruppen oder Images) festgelegt.

2.2 Fehlerbehandlung

Um die erfolgreiche Ausf�hrung einer Funktion zu �berpr�fen werden verschiedene Strategien verfolgt:

1. �berpr�fung des R�ckgabewerts einer Funktion

Diese Strategie wird bei AWS-spezifischen Funktionen angewandt. Bei Aufruf eines AWS-Kommandos (z.B. aws elb create-load-balancer) wird
bei Erfolg der R�ckgabewert 0 zur�ckgegeben und bei Vorkommen eines Fehlers der R�ckgabewert 1.

2. �berpr�fen des HTTP-Codes

Bei Aufruf eines curl-Kommandos wird die Option -w "\n%{http_code}" mit�bergeben. Dabei werden Response- und HTTP-Code auf zwei Zeilen
aufgeteilt und damit erm�glicht, dass nach Aufruf der Funktion die Zuordnung zu zwei verschiedenen Variablen erfolgt.

2.3 Meldungsausgabe

Innerhalb von Methoden wird der Nutzer �ber Erfolg oder Misserfolg benachrichtigt. Die Ausgabe erfolgt �ber den
Standardfehlerkanal "stderr" (Workaround).

2.4 Tail mit tmux

Beim Verwenden des Parameters --tail werden automatisch relevante Log-Dateien angezeigt.
Der Befehl ist in Kombination mit dem Parameter --public-dns-name oder auch mit dem Szenario
--instance-with-load-balancer verwendbar. Zur Ausf�hrung muss das Skript in einer tmux-Session
ausgef�hrt werden.

Beispiele:
./aws-setup.sh --tail --public-dns-name ec2-xxx.eu-west-2.compute.amazonaws.com
Zeigt relevante Log-Dateien der Instanz mit dem DNS Namen "ec2-xxx.eu-west-2.compute.amazonaws.com" an.

./aws-setup.sh --tail --instance-with-load-balancer
Zeigt relevante Log-Dateien der Instanz die gerade erstellt wird, ab dem Zeitpunkt der Aufnahme der SSH-Verbindung

3. TODO

3.1 Fehlerbehebungen

- Nutzer erstellen scheitert bisher mit "Precondition failed" (Nutzer-
  name erf�llt Richtlinie nicht)

3.2 Verbesserungen

- run-instance Funktion nach "aws_functions.sh" faktorisieren
- Anpassung der Dateierzeugung an Linux ("crlf" ersetzen)
- Momentan wird der Admin-Nutzer als Eventersteller verwendet.
  Die Rechtevergabe an einen anderen Nutzer �ber die REST-API
  muss noch implementiert werden.

3.2 Erweiterungen

- list-Funktion mit Anzeige aller Instanznamen und deren DNS, Instanz-ID, etc. damit
  --tail nicht die Web-GUI erfordert
- weitere Szenarios hinzuf�gen (Redeployment, R�ckmigration in Archiv, etc.)
- regionspezifisches Sourcing von Dateien mit Variablen (Auskommentieren nicht mehr notwendig)
- automatische Kontext�bernahme nach tmux 


Getestet unter Microsoft Windows [Version 10.0.10586]:
- Cygwin 2.8.2(0.313/5/3)
- aws-cli/1.11.129
- tmux 2.8.2(0.313/5/3)
- openssh 7.5p1-1
- wget 1.19.1
- curl 7.53.0

Funktioniert (Route53-Eintrag Erstellung konnte nicht getestet werden)

Getestet unter Microsoft Windows [Version 6.3.96001]:
- Cygwin 2.9.0(0.318/5/3)
- aws-cli/1.11.166
- tmux 2.4
- openssh 7.4p1
- wget 1.19.1
- curl 7.55.1-1

Funktioniert (Route53-Eintrag Erstellung konnte nicht getestet werden)
