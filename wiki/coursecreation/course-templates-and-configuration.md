# Scope

* TODO


# Model

We defined the model for course templating and configuration. In addition the various model elements have defined masterdata and other properties.

## Mark template have the following properties

* Appearance
* Mark templates are immutable


## Mark properties

* Appearance
* Optional positioning information
    * Fixed position
    * Tracking device identifier
* Mark properties are mutable


### Course template

* Set of mark templates to be created
* Optional repeatable part
* Course templates may contain mark templates that aren't part of the defined sequence
* For every mark template that is part of the sequence, a distinct role name need to be provided
    * Distinct means it is a bijective mapping of marks in the sequence and role names
    * The role name may the same as the mark template being mapped


### Course configuration

* In memory representation with optional references to domain objects
    * If freely created, no references to domain objects exist yet
    * When created based on a course template or regatta course, the created elements are based on the elements of the originating source
    * While changing a course configuration in the UI, a mix of different definitions may show up

* A sequence of waypoints
    * Including an optional repeatable part

* Marks in a course configuration can be freely created or based on:
    * Regatta mark
    * Mark template
    * Mark properties

* Can be constructed from or saved to
    * Course template
    * Regatta course

* When stored in one of the possible forms, all required domain objects are required to be created automatically. This means, all data required needs to be provided in the configuration as well.
    * This is defined in contrast to directly creating regatta courses, where all required marks and tracking information need to get created in distinct requests.

* Optionally be based on a course template
    * As long as the sequence of a course isn't changed with regard to the course template, a course is meant to be based on the original course template. This reference may be transferred through update steps.
    * As long as a course configuration is based on a course template, its structure can be mapped to the originating course template (including potentially existing repeatable parts)


# Definition of various aspects for handling the source configuration model

## Scope when creating a course configuration

When designing courses, the following aspects and data sources need to get included in the course configuration.

1. Regatta context
    * Designing a course in a regatta context
    * Designing a course independently of a regatta

2. Regatta specifics
    * Define course for a regatta without pre existing marks
    * Define course for a regatta with pre existing marks
    * Updating a regatta course

3. Course Template context
    * A course configuration based on a course template
    * A course configuration not based on a course template
    * A course configuration with changed sequence after a course template was applied


## Rules for the construction of mark configurations

### Construction from course template

* For each regatta mark, a mark configuration needs to be created.
* If a mark was already mapped to a given role in an existing course, this is used. If there are multiple usages, the most recent ones is used. A mark can only be mapped to one role within one regatta.
* Create mark configuration objects for all mark templates not existing in the regatta. Those are subsequently associated to the remaining roles.
* For each role that remains unmapped (those ones a user unmapped manually or by side effects of editing the sequence), a new entry from the mark template is created and mapped to the role.
* Optionally, for all mark configurations created from mark templates the mark properties object needs to be mapped based on the last use.

### Construction from regatta course

* For each regatta mark, a mark configuration needs to be created.
* The mapping of mark configurations to roles is based on the optional mapping of a mark to a role in a course. 
    * If the course is based on a course template, the set of roles needs to match the set of roles in the associated course template.
    * If the course is not based on a course template, the role mapping is created from the course defaulting to the names of the marks.


## Mark configuration types and their carried information

* Mark template
    * Appearance is defined by mark template
    * Optional positioning information to be used upon mark creation
* Regatta mark
    * Appearance is defined by mark. Changing is not supported which means in this case, a new mark needs to be defined in any supported way as replacement for new courses, while the old definition remains for old courses.
    * Optional positioning information (to update the regatta mark)
* Freestyle mark definition
    * TODO discuss: optional mark template reference
    * optional mark properties reference



## Roles and their significance

When creating a course from a course template, the set of marks to be created is not limited to the ones used in the sequence. A set of spare marks is explicitly required to be exchanged by another mark without effectively changing the sequence. Especially using a mark for another waypoint as defined by the template, introduces an ambiguity to this model. Another ambiguity occurs when creating a new mark for a while using the original one in another position of the sequence. This requires to distinct:

1. Originating mark template definition
2. Originating position in the sequence/waypoint

As 1. is the meaning of creating marks from mark templates this requires the introduction of a new concept that defines "slots" to be filled when configuring a course based on a course template.

Therefore we defined roles to distinctly name marks used in a sequence of waypoints.

In the UI model (while not having the concept of roles) it would be possible to just swap all uses of a mark with another one without modifying the sequence. Especially the case mentioned above would cause an ambiguity regarding how the mapping of marks of the course template is meant. The definition of roles in fact removes the dependency of the sequence to mark templates.

Said that, it is necessary to provide a distinct and complete (bijective) mapping for mark templates to roles used in the sequence of a course template. Any regatta course that provides an equivalent and complete mapping for its marks is a valid candidate to reconstruct the course template definition without implying a data loss.


## Categorizing marks in course configurations

* Mark configurations used in the course sequence
* Existing regatta marks not used in the course sequence
* Additional Mark configurations not used in the sequence


## Definition of valid actions for mark configurations

* TODO possible actions and their mapping to the course configuration


# Use Cases

TODO: do we need this?
