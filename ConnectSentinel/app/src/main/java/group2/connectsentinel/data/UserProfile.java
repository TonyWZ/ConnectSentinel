package group2.connectsentinel.data;

public class UserProfile {

    private long id;
    private String name;
    private long emergencyContactId;
    private boolean abilityMedical;
    private boolean abilityCrime;
    private String password;

    public UserProfile(long id, String name, long emergencyContactId, boolean abilityMedical, boolean abilityCrime, String password) {
        this.id = id;
        this.name = name;
        this.emergencyContactId = emergencyContactId;
        this.abilityMedical = abilityMedical;
        this.abilityCrime = abilityCrime;
        this.password = password;
    }

    public long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public long getEmergencyContactId(){
        return emergencyContactId;
    }

    public boolean getAbilityMedical(){
        return abilityMedical;
    }

    public boolean getAbilityCrime(){
        return abilityCrime;
    }

    public String getPassword() {return password;}

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmergencyContactId(long contact) {
        this.emergencyContactId = contact;
    }

    public void setAbilityMedical(boolean medicalAbility) {
        this.abilityMedical = medicalAbility;
    }

    public void setAbilityCrime(boolean crimeAbility) {
        this.abilityCrime = crimeAbility;
    }

    public void setPassword(String newPW) {
        this.password = newPW;
    }

}
