# Notice of Race Complex Scoring Implementation

This document describes the implementation of a new ScoringScheme for the sailing live scoring application that handles the complex scoring requirements from Notice of Race section 24.

## Overview

The implementation consists of three main components:

1. **NoticeOfRaceComplexScoringScheme** - The main scoring scheme implementation
2. **NoticeOfRaceResultDiscardingRule** - Custom discard rule handling
3. **NOTICE_OF_RACE_COMPLEX_SCORING** - New ScoringSchemeType enum value

## Key Features Implemented

### 1. Race Type Handling (24.3)

#### Course Racing (24.3.1)
- Standard low-point scoring (1st = 1pt, 2nd = 2pt, etc.)
- One race = one race in series

#### Sprint Racing (24.3.2)
- **When racing in heats**: Custom scoring table
  - 1st = 1pt, 2nd = 3pts, 3rd = 5pts, 4th = 7pts, 5th = 9pts, etc.
  - Pattern: score = 2*rank - 1
- **When not in heats**: Standard low-point scoring
- Detection via naming convention: "sprint" + ("heat" OR "group" OR split fleets)

#### Marathon Racing (24.3.3)
- Represented as **two separate RaceColumn objects**
- Qualifying Series: Position ÷ number of groups, applied to both races
- Final Series: Direct position applied to both races
- Factor = 1.0 (since handled by separate columns)

### 2. Medal Series Implementation (24.9)

#### Structure
- **Quarter Final**: 5th-8th from Opening Series
- **Semi Final**: Top 2 from QF + 3rd-4th from Opening Series
- **Grand Final**: Top 2 from SF + 1st-2nd from Opening Series

#### Match Points System (24.9.3)
- **Target**: First to 2 match points wins
- **Carry-forward from Opening Series**:
  - 1st place → 1 match point
  - 2nd place → 0.5 match point
  - 0.5 becomes 1 if competitor wins any Grand Final race

#### Tie-Breaking (24.9.3.6)
1. Number of match points
2. Score in last race of Grand Final
3. Opening Series rank (previous stage where tied competitors sailed together)

### 3. Complex Discard Rules (24.4)

#### Single Fleet (24.4.1)
- 1-2 races: 0 discards
- 3-7 races: 1 discard
- 8-15 races: 2 discards
- 16+ races: 3 discards
- **BFD Limitation**: Max 1 BFD score in Sprint Racing may be excluded

#### Split Fleet (24.4.2)
**Qualifying Series**:
- 1-2 races: 0 discards
- 3-7 races: 1 discard
- 8-10 races: 2 discards

**Final Series**:
- 1-2 races: 0 discards
- 3-7 races: 1 discard
- 8+ races: 2 discards

**BFD Limitation**: Max 1 BFD score per series in Sprint Racing

### 4. Fleet-Based Ranking (24.8)
- Gold > Silver > Bronze fleet ordering
- Handled by existing fleet ordering system (1/2/3)
- Assigned by tracking provider via TrackedRace

### 5. Age Division Support (24.2)
- U23 division extraction handled by `RegattaLeaderboardWithEliminations`
- Not implemented in ScoringScheme (architectural separation)

## Implementation Details

### Class Structure

```java
public class NoticeOfRaceComplexScoringScheme extends LowPoint {
    // Sprint Racing scoring table (1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47)
    private static final Map<Integer, Integer> SPRINT_RACING_HEAT_SCORES;
    
    // Match points target for Grand Final
    private static final int TARGET_MATCH_POINTS_TO_WIN = 2;
}
```

### Key Method Overrides

1. **`getScoreForRank()`** - Handles Sprint Racing custom scoring vs standard low-point
2. **`isMedalWinAmountCriteria()`** → `true` - Enables match points system
3. **`isCarryForwardInMedalsCriteria()`** → `true` - Enables Opening Series carry-forward
4. **`getScoreFactor()`** → `1.0` - All races have factor 1.0 (Marathon handled by separate columns)
5. **`compareByMedalRacesWon()`** - Custom match point comparison (first to 2 wins)
6. **`getWinCount()`** - Handles fractional match points (0.5 → 1 conversion)
7. **`compareByLastMedalRacesCriteria()`** - Complex Medal Series tie-breaking

### Discard Rule Implementation

```java
public class NoticeOfRaceResultDiscardingRule implements ResultDiscardingRule {
    // Threshold arrays for different formats
    private static final int[] SINGLE_FLEET_THRESHOLDS = {3, 8, 16};
    private static final int[] QUALIFYING_SERIES_THRESHOLDS = {3, 8};
    private static final int[] FINAL_SERIES_THRESHOLDS = {3, 8};
}
```

## Integration Points

### Factory Integration
- Added `NOTICE_OF_RACE_COMPLEX_SCORING` to `ScoringSchemeType` enum
- Updated `DomainFactoryImpl.createScoringScheme()` to instantiate new scheme

### Framework Compatibility
- Extends existing `LowPoint` base class
- Uses existing `LeaderboardTotalRankComparator` for ranking
- Compatible with existing fleet ordering system
- Integrates with `MaxPointsReason` for penalty handling

## Usage Example

```java
// Create the scoring scheme
ScoringScheme scheme = domainFactory.createScoringScheme(
    ScoringSchemeType.NOTICE_OF_RACE_COMPLEX_SCORING);

// Create custom discard rule
ResultDiscardingRule discardRule = new NoticeOfRaceResultDiscardingRule();

// Apply to leaderboard
leaderboard.setScoringScheme(scheme);
leaderboard.setResultDiscardingRule(discardRule);
```

## Race Column Setup

### Marathon Races
```java
// Create two separate race columns for one Marathon race
RaceColumn marathon1 = createRaceColumn("Marathon Race 1A");
RaceColumn marathon2 = createRaceColumn("Marathon Race 1B");
// Both have factor 1.0, represent the same physical race
```

### Sprint Racing in Heats
```java
// Name must contain "sprint" and indicate heats
RaceColumn sprintHeat = createRaceColumn("Sprint Racing Heat A");
sprintHeat.setHasSplitFleets(true); // Indicates heats
```

### Medal Series
```java
// Quarter Final
Series quarterFinal = createSeries("Quarter Final");
quarterFinal.setMedal(true);

// Semi Final  
Series semiFinal = createSeries("Semi Final");
semiFinal.setMedal(true);

// Grand Final with carry-forward
Series grandFinal = createSeries("Grand Final");
grandFinal.setMedal(true);
grandFinal.setStartsWithZeroScore(true);

RaceColumn carryForward = createRaceColumn("Carry Forward");
carryForward.setCarryForward(true);
carryForward.setMedalRace(true);
```

## Testing Considerations

### Test Scenarios
1. **Sprint Racing Scoring**: Verify custom scoring table vs standard low-point
2. **Marathon Race Doubling**: Ensure two columns represent one physical race
3. **Match Points System**: Test carry-forward and 0.5 → 1 conversion
4. **Complex Discards**: Test all discard patterns and BFD limitations
5. **Medal Series Progression**: Test QF → SF → GF advancement
6. **Fleet Ranking**: Test Gold > Silver > Bronze ordering
7. **Tie-Breaking**: Test all tie-breaking criteria in correct order

### Edge Cases
1. Fractional match points handling
2. BFD discard limitations in mixed race types
3. Split fleet vs single fleet detection
4. Medal Series with incomplete participation
5. Marathon races in different series types

## Future Enhancements

1. **Race Type Attributes**: Replace naming convention with explicit RaceColumn attributes
2. **Custom ScoringSchemeType**: Consider more specific enum value if needed
3. **Marathon Race Linking**: Explicit linking between related Marathon race columns
4. **Advanced Medal Series**: Support for more complex Medal Series formats
5. **Validation**: Add validation for proper race column setup

## Conclusion

This implementation provides a comprehensive solution for the complex Notice of Race scoring requirements while maintaining compatibility with the existing sailing analytics framework. The modular design allows for easy testing and future enhancements while leveraging existing infrastructure for fleet management, competitor tracking, and leaderboard calculation.
