# ORC/IRC support

## Rationale

Beside the One-Design racing (where all boats have identical or very similar designs or models) there is another huge category of regattas called 'handicap sailing' where the boats are not identical and therefore have different race conditions. In order to make the boats nevertheless comparable several organisations (ORC/IRC/YardStick) introduced complex rule systems to calculate individual 'handicaps' for the boats. As a result it's even more difficult to follow these regattas and to present them in an attractive way to the audience.
However, as the typical boats in this regatta category are rather big boats the audience and the owner of the boats might be an interesting target group.

## Purpose

Provide solutions to help organizations operating ORC/IRC/Yardstick based regattas to manage them and to enable  live moderation of such regattas to make them more attractive for the media, spectators and sailors.

## Features

* Support for different handicap scoring systems (live calculation, live leaderboard)
    * Time On Time
    * Time On Distance
    * Performance Curve
    * Performance Line
* Visualization of live boat positions/ranking incorporating the handicap
* Support of satellite trackers for long distance races
* Optional: Access on board sensors of racing boats (e.g. for wind)

## Description

It seems to be natural that there are many sailors who want to compete against each other even if they sail different boat models. The reason is often simply that they can't find enough other sailors having exactly the same boat as so many different boat models does exist (think of a club where sailors want to have a club regatta each week or owners of real big expensive boats which are unique). The main difficulty is to create rules which makes the boats somehow comparable but keep the effort low to ensure the right application of these rules. Over time several rule systems have been created which are managed by internationally accepted organizations (ORC, IRC).
All these rule systems calculate a 'handicap' for each boat which is applied to boat during the racing.



## Synergies
There are synergies with the Smartphone Tracking project as well as the Sailing Analytics in general.
Many core functions of the existing Sailing Analytics can be reused (e.g GPS and wind tracking, race viewer, live leaderboard calculation, etc.), but must be adapted to the specifics of the handicap sailing.
The Smartphone Tracking would also benefit from the project as many small regattas are handicap races.

## Risks
One risk is the lack of understanding the real world problems of handicap regattas. So we can't prove that the listed features address the right problem spaces. To minimize that risk we should attend some handicap regattas.
Another risk is the that we don't know how big the investment must be to reach a point where we can really change the way handicap regattas are presented.

## Information Sources
ORC: http://orc.org/
IRC: http://www.ircrating.org/

