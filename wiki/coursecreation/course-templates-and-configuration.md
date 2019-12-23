# Motivation and scope

Previously, marks as well as courses were exclusively bound to the regatta context. In addition the definition of courses was possible with existing regatta marks only. Having this kind of model, it is not easily possible to transfer courses including their respective tracking information to another regatta. In addition on the fly changes while doing so is another inconvenience.

Therefore a regatta independent model is required that allows the definition of course sequences as well as tracking information. This new model is defined below and allows an easy bootstrapping of courses in different regatta contexts but based on the same masterdata.

The new model is not intended to replace the regatta specific course and mark model but rather extends it to cover the newly emerged use cases. In addition the new model - while being able to map all cases - allows an entirely free definition of courses using just a subset of the possibilities in the UI.


# Regatta independent course model

We defined the model for course templating and configuration. In addition the various model elements have defined masterdata and other properties.

## Mark template have the following properties

A mark template defines the appearance of a mark in a regatta independent representation. Mark templates represent marks in the waypoint sequence of a course template.

Mark templates have the following properties:

* Provide an appearance
* Mark templates are immutable


## Mark properties

Mark properties also define the appearance of a mark. Despite the appearance it may contain a reference to a tracking device or a fixed mark position. They can be used to represent a catalogue of resusable mark definitions to describe real world marks or to supply a box of tracking devices.

Mark properties have the following properties:

* Provide an appearance
* Optionally include positioning information. either of:
    * Fixed position
    * Tracking device identifier
* Mark properties are mutable


### Course template

A course template can be used to create a course based on mark templates and a sequence of waypoints. The sequence of waypoints may contain a repeatable sub sequence of waypoints which will insert repeating mark sequences for the number of laps specified when creating a course.

* Set of mark templates to be created
* Optional repeatable part
* Course templates may contain mark templates that aren't part of the defined sequence
* For every mark template that is part of the sequence, a distinct mark role need to be provided
    * Distinct means it is a bijective mapping of marks in the sequence and mark roles 
    * The mark role may the same as the mark template being mapped


### Mark role

A mark role defines the purpose of a mark used in the waypoint sequences of a regatta course or course template and allows users to swap out marks or mark templates without changing the the effective waypoint sequence. Having this, a course template and regatta course may define a compatible waypoint sequence while being based on different mark definitions.


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


## Mark configuration types and their carried information

* Mark template based configuration
    * Appearance is defined by a mark template only
    * Optional positioning information to be used upon mark creation
* Regatta mark based configuration
    * Appearance is defined by mark. Changing is not supported which means in this case, a new mark needs to be defined in any supported way as replacement for new courses, while the old definition remains for existing courses.
    * Type is only available if the course is created in a regatta context
    * Optional positioning information (to update the regatta mark)
* Mark properties based configuration
    * Appearance is defined by mark properties defaulting to the definition of the mark template if none is defined in the properties
    * Optional positioning information to be used upon mark creation (defaults to the positioning information carried by the mark properties if not given)
* Freestyle mark configuration
    * Appearance properties are required to be fully defined
    * Optional mark template reference
    * Optional mark properties reference
    * Optional positioning information to be used upon mark creation


## Rules for the construction of mark configurations

### Last usage based matching rules

When creating course configurations, it is tried to match regatta marks as well as mark properties to mark templates based on the last usage. Because of the fact that a regatta mark or mark properties could be associated to different mark templates or roles historically, it could be the best match for more than one mark template.

Example: A mark M was associated to role R1 in race 1. M was associated to role R2 in race 2. The roles R1 and R2 are distinctly used in one of the races. This means M would match both, R1 and R2. For R1 and R2 the only match would be M. In a course template including both R1 and R2 we can't match M to both roles. In this case it is checked if the best match in reverse direction leads to the same role. In this example, the latest (best) match for M is R2. Given that, R1 will not be matched to a mark at all.

In general this means: A match based on last usage is only counted as match if both elements (e.g. Role and mark) reference each other as the only or latest match.

This rule is always applied for at least the following cases:
* Matching regatta marks by usage of their associated roles to mark templates
* Matching marks properties by usage of their associated roles to mark templates
* Matching marks properties by direct usage for mark templates


### Construction from course template

Construction from a course template means, a course template is directly loaded. No regatta course is involved in this process but as the course template may be loaded in a regatta context, existing regatta marks may be included in the course configuration.

* If the course configuration is created in a regatta context
    * If the configuration is created in a regatta context, for each existing regatta mark, a mark configuration needs to be created.
    * For any mark template that is part of the course template a matching regatta mark is searched as replacement. As the first priority, last usage based matching (see above) is done on roles being associated to regatta marks as well as mark templates. For any mark template that could not be matched by role, a regatta mark can be associated if it was created based on that mark template.
    * Create mark configuration objects for all mark templates not existing in the regatta. Those are subsequently associated to the remaining roles.
* If the course template is created without a regatta context
    * Create mark configuration objects for all mark templates being part of the course template
* For each mark template based configuration associated to a role, mark properties objects are searched as replacement based on the last usage (see above) of the mark properties for the role.
* For each remaining mark template based configuration, mark properties objects are searched as replacement based on the last usage (see above) of the mark properties for the nmark template.
* The waypoint sequence of the course template is mapped to the course configuration by replacing the mark templates by the mark configurations identified in the steps before.
* The potentially existing repeatable part is exactly the one being part of the course template


### Construction from a race of a regatta

When loading a course from a race of a regatta, several constellations may occur:

1. No course is associated to a race at all
2. A course is associated to the race without being based on a course template. This can have multiple reasons:
    * The course was created freely without using a course template
    * The course sequence was changed so that it doesn't match the course template anymore
    * The course template has been deleted in the meanwhile
3. A course is based on a course template.

In case a course template is referenced by a course, additional checks are necessary to distinct 2. and 3.

Based on those effective constellations, the course configuration is constructed the following way:

* For each regatta mark, a mark configuration is created.
* In case of 1. an empty waypoint sequence is defined in the resulting course configuration
* In case of 2., the waypoint sequence is exactly mapped using the mark configurations being created from the marks while no repeatable part is defined in the resulting course configuration
* In case of 3.
    * For each role of the course, a 1:1 match exists in the course template.
    * Having said that, the waypoint sequence of the course template can be used in the course configuration by doing a replacement based on the roles.
    * There may be rare cases where a course template with repeatable part is used to construct a course with 0 laps. This can cause roles and mark templates of the course template not having a match in the course. Those missing mark templates are required to explicitly be added to the course configuration.
    * The repeatable part is directly taken from the course template. If a repeatable part is available for the course template, the effective lap count can be calculated based on the number of waypoints in the course and course template.
    * Any mark template that is not mapped to a regatta mark already is tried to be mapped to mark properties using last usage based matching (see above).


## Roles and their significance

When creating a course from a course template, the set of marks to be created is not limited to the ones used in the sequence. A set of spare marks is explicitly required to be exchanged by another mark without effectively changing the sequence. Especially using a mark for another waypoint as defined by the template, introduces an ambiguity to this model. Another ambiguity occurs when creating a new mark for a while using the original one in another position of the sequence. This requires to distinct:

1. Originating mark template definition
2. Originating slot in the sequence/waypoint

As 1. is the meaning of creating marks from mark templates this requires the introduction of a new concept that defines "slots" to be filled when configuring a course based on a course template.

Therefore we defined mark roles to distinctly identify slots in a sequence of waypoints.

In the UI model (while not having the concept of roles) it would be possible to just swap all uses of a mark with another one without modifying the sequence. Especially the case mentioned above would cause an ambiguity regarding how the mapping of marks of the course template is meant. The definition of roles in fact removes the dependency of the sequence to mark templates.

Said that, it is necessary to provide a distinct and complete (bijective) mapping for mark templates to roles used in the sequence of a course template. Any regatta course that provides an equivalent mapping for its marks is a valid candidate to reconstruct the course template definition without implying a data loss. This especially included the reconstruction of the repeatable part and effective lap counts.


## Categorizing marks in course configurations

* Mark configurations used in the course sequence
* Existing regatta marks not used in the course sequence
* Additional Mark configurations not used in the sequence


## Definition of valid actions for mark configurations

* Mark template based configuration
    * Changing appearance/attaching tracking information: Replaces it with a freestyle configuration having a mark template reference
    * Select mark properties: Replaces it with a freestyle configuration having both, a mark template and mark properties reference
* Regatta mark based configuration
    * Attaching tracking information: Updates the existing configuration with tracking information
* Mark properties based configuration
    * Changing appearance/attaching tracking information: Replaces it with a freestyle configuration keeping the mark properties and optional mark template reference
* Freestyle configuration
    * Changing appearance/attaching tracking information: Updates the existing configuration accordingly keeping any mark template or mark properties reference
    
New configurations may be created (e.g. by adding a new mark to the sequence or course configuration) as follows:
* Freestyle configuration: Adding a new mark
* Mark properties based configuration: By selecting a mark from the mark properties inventory
* TODO Mark template based configuration?

In addition, for marks used in the course sequence, a new freestyle mark configuration may be created replacing the original configuration in the role. This causes the previous configuration to remain unused but not to be deleted automatically.


## Tracking usages

When choosing a course template, as many parts of the configuration should get preselect as possible. In addition, when loading a course that was based on a template, also as much configuration as possible should be reconstructed. This requires us to track several usages:

* Usage of mark properties to roles: Allows suggestions of mark properties for the role in question based on last usage (globally limited by READ permissions)
* Usage of mark properties to mark templates: Allows further suggestions of mark properties for marks referenced in course templates (globally limited by READ permissions)
* Originating mark template for regatta mark: This allows to only create those marks based on templates that are not already present
* Originating mark properties for regatta mark: This allows the user to export tracking information attached to a mark also to the originating mark properties.
* Mapping of marks to roles in a regatta course: This allows to suggest mapping of either regatta marks or mark templates based on last usage to roles when creating a configuration based on a course template.
