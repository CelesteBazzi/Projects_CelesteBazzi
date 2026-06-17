package pokemon.simulation;
import com.fasterxml.jackson.annotation.JsonProperty;


public class PokemonCard {
@JsonProperty("Trainer_Name")
private String Trainer_Name;
@JsonProperty("Pokemon_Name")
private String Pokemon_Name;
@JsonProperty("Type") 
private String Type;

@JsonProperty("HP")
private double HP;

@JsonProperty("Name_Attack")
private String Name_Attack;

@JsonProperty("Attack")
private double Attack;

@JsonProperty("Effect")
private String Effect;

@JsonProperty("Energy1")
private String Energy1;

@JsonProperty("Energy2")
private String Energy2;

@JsonProperty("Energy3")
private String Energy3;

@JsonProperty("Energy4")
private String Energy4;

@JsonProperty("Weakness")
private String Weakness;

@JsonProperty("Resistance")
private String Resistance;

@JsonProperty("Retreat")
private double Retreat;

@JsonProperty("N_cards")
private int N_cards;

@JsonProperty("Num_Energy")
private double Num_Energy;

@JsonProperty("KeyWord_Effect")
private String KeyWord_Effect;


@JsonProperty("azione")
private String azione;

@JsonProperty("POS_Tags_stanza")
private String POS_Tags_stanza;

@JsonProperty("POS_Lemmatized")
private String POS_Lemmatized;

@JsonProperty("chi")
private String chi;

@JsonProperty("a_chi")
private String a_chi;

@JsonProperty("cosa")
private String cosa;

@JsonProperty("azione2")
private String azione2;

private double currentHP;

//empty builder requested by Jackson(for jason data)
public PokemonCard() {}

public PokemonCard(PokemonCard original) {
    this.Trainer_Name = original.Trainer_Name;
    this.Pokemon_Name = original.Pokemon_Name;
    this.Type = original.Type;
    this.HP = original.HP;
    this.currentHP = original.currentHP; 
    this.Name_Attack = original.Name_Attack;
    this.Attack = original.Attack;
    this.Effect = original.Effect;
    this.Energy1 = original.Energy1;
    this.Energy2 = original.Energy2;
    this.Energy3 = original.Energy3;
    this.Energy4 = original.Energy4;
    this.Weakness = original.Weakness;
    this.Resistance = original.Resistance;
    this.Retreat = original.Retreat;
    this.N_cards = original.N_cards;
    this.Num_Energy = original.Num_Energy;
    this.KeyWord_Effect = original.KeyWord_Effect;
    this.azione = original.azione;
    this.POS_Tags_stanza = original.POS_Tags_stanza;
    this.POS_Lemmatized=original.POS_Lemmatized;
    this.chi = original.chi;
    this.a_chi = original.a_chi;
    this.cosa = original.cosa;
    this.azione2 = original.azione2;
}


@JsonProperty("HP")
public void setHP(double HP) {
this.HP = HP;
this.currentHP = HP; //initialize current HP
}

public double getHP() {
return currentHP;
}

public double getHPOriginal() {
    return HP;
}

public int getN_cards(){
    return N_cards;
}

public void receiveDamage(double dmg) {
this.currentHP -= dmg;
if (this.currentHP < 0) this.currentHP = 0;
}

public boolean isKnockedOut() {
return currentHP <= 0;
}

public double getAttack() {
return Attack;
}

public void setAttack(double attack) {
this.Attack = attack;
}

public String getPokemon_Name() {
return Pokemon_Name;
}

public void setPokemon_Name(String pokemon_Name) {
Pokemon_Name = pokemon_Name;
}

public String getTrainer_Name() {
return Trainer_Name;
}

public String getName_Attack() {
    return Name_Attack;
    }

public void setTrainer_Name(String trainer_Name) {
Trainer_Name = trainer_Name;
}

public String getType_Card() {
    return Type;
    }

public String getAzione() {
    return azione;
}
public String getAzione2() {
    return azione2;
}
public String getChi() {
    return chi;
}
public String getAChi() {
    return a_chi;
}
public String getCosa() {
    return cosa;
}
public String getEffect() {
    return Effect;
}

public String getKeyWord_Effect() {
    return KeyWord_Effect;
}

//put because otherwise getRemainingCards in HandPlayer would not report the data in full (is a function to verify the values)
@Override
public String toString() {
    return "[Nome: " + getPokemon_Name() +
        ", Tipo: " + getType_Card() +
        ", HP: " + getHP() +
        ", Attacco: " + getAttack() + "]";
}

//function to view if a card is a pokemon
public boolean isPokemon(String Pokemon_Type){
    return ("Colorless".equalsIgnoreCase(Pokemon_Type) || "Darkness".equalsIgnoreCase(Pokemon_Type) 
    || "Dragon".equalsIgnoreCase(Pokemon_Type)  || "Fighting".equalsIgnoreCase(Pokemon_Type) 
    || "Fire".equalsIgnoreCase(Pokemon_Type) || "Grass".equalsIgnoreCase(Pokemon_Type) 
    || "Lightning".equalsIgnoreCase(Pokemon_Type) || "Metal".equalsIgnoreCase(Pokemon_Type) 
    || "Psychic".equalsIgnoreCase(Pokemon_Type) || "Water".equalsIgnoreCase(Pokemon_Type) );

}
    
}