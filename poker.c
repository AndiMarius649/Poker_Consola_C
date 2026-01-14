#include<stdio.h>
#include<windows.h>
#include<stdlib.h>
#include<time.h>
#include<string.h>

typedef struct{

    int valoare;
    char suita;
}Carte;


const char* simbolSuita(char s)
{
    if (s == 'S') return "♠";
    if (s == 'H') return "♥";
    if (s == 'D') return "♦";
    if (s == 'C') return "♣";
    return "?";
}

const char* simbolValoare(int v)
{
    const char* vals[] = {"0","1","2","3","4","5","6","7","8","9","10","J","Q","K","A"};
    return vals[v];
}
const char* linieCarte(int linie, const char* numar, const char* suita)
{
    if(linie == 0) return "┌─────────┐";

    if(linie == 1) {
        if(strcmp(numar,"10")==0) return "│ 10      │";
        if(strcmp(numar,"A")==0)  return "│ A       │";
        if(strcmp(numar,"K")==0)  return "│ K       │";
        if(strcmp(numar,"Q")==0)  return "│ Q       │";
        if(strcmp(numar,"J")==0)  return "│ J       │";
        static char linieNum[12];
        sprintf(linieNum, "│ %-2s      │", numar);
        return linieNum;
    }

    if(linie == 2) return "│         │";

    if(linie == 3) {
        if(strcmp(suita,"♠")==0) return "│    ♠    │";
        if(strcmp(suita,"♥")==0) return "│    ♥    │";
        if(strcmp(suita,"♦")==0) return "│    ♦    │";
        if(strcmp(suita,"♣")==0) return "│    ♣    │";
    }

    if(linie == 4) {
        if(strcmp(numar,"10")==0) return "│      10 │";
        if(strcmp(numar,"A")==0)  return "│       A │";
        if(strcmp(numar,"K")==0)  return "│       K │";
        if(strcmp(numar,"Q")==0)  return "│       Q │";
        if(strcmp(numar,"J")==0)  return "│       J │";
        static char linieNum[12];
        sprintf(linieNum, "│       %-2s│", numar);
        return linieNum;
    }

    if(linie == 5) return "└─────────┘";

    return "";
}
void afiseazaMana(Carte mana[5])
{
    for(int linie = 0; linie < 6; linie++)
    {
        for(int i = 0; i < 5; i++)
        {
            printf("%s ",linieCarte(linie,simbolValoare(mana[i].valoare),simbolSuita(mana[i].suita)));
        }
        printf("\n");
    }
}


void main(){

    SetConsoleOutputCP(CP_UTF8);




    int valori[] = {2,3,4,5,6,7,8,9,10,11,12,13,14}; // 11=J,12=Q,13=K,14=A
    const char* suite[] = {"♠", "♥", "♦", "♣"};

    Carte pachet[52];


    int index = 0;
    for(int s=0; s<4; s++) {
        for(int v=0; v<13; v++) {
            pachet[index].valoare = valori[v];
            if(s==0)
            pachet[index].suita = 'S';
            if(s==1)
            pachet[index].suita = 'H';
            if(s==2)
            pachet[index].suita = 'D';
            if(s==3)
            pachet[index].suita = 'C';
            index++;
        }
    }
    srand(time(NULL));
    for(int i=51; i>0; i--) {
        int j = rand() % (i+1);
        Carte tmp = pachet[i];
        pachet[i] = pachet[j];
        pachet[j] = tmp;
    }
    Carte manaJucator[6];
    for(int i=0; i<5; i++){
    manaJucator[i] = pachet[i]; // primele 5 cărți din pachetul amestecat
    }
    afiseazaMana(manaJucator);

    printf("Cartile tale: %s%s  %s%s  %s%s  %s%s  %s%s\n",simbolSuita(manaJucator[0].suita),simbolValoare(manaJucator[0].valoare),simbolSuita(manaJucator[1].suita),simbolValoare(manaJucator[1].valoare),simbolSuita(manaJucator[2].suita),simbolValoare(manaJucator[2].valoare),simbolSuita(manaJucator[3].suita),simbolValoare(manaJucator[3].valoare),simbolSuita(manaJucator[4].suita),simbolValoare(manaJucator[4].valoare));
    printf("Ce carte schimbi? (0 pt niciuna): ");

    int schimbata;

    scanf("%d",&schimbata);
    if(schimbata!=0){
    manaJucator[schimbata-1]=pachet[6];

    afiseazaMana(manaJucator);
    printf("Pachetul tau contine acum: %s%s  %s%s  %s%s  %s%s  %s%s\n",simbolSuita(manaJucator[0].suita),simbolValoare(manaJucator[0].valoare),simbolSuita(manaJucator[1].suita),simbolValoare(manaJucator[1].valoare),simbolSuita(manaJucator[2].suita),simbolValoare(manaJucator[2].valoare),simbolSuita(manaJucator[3].suita),simbolValoare(manaJucator[3].valoare),simbolSuita(manaJucator[4].suita),simbolValoare(manaJucator[4].valoare));
    }

    int fValoare[15]={0};
    int fSuita[4]={0};

    for(int i=0; i<5; i++){
    fValoare[manaJucator[i].valoare]++;
    if(manaJucator[i].suita=='S') fSuita[0]++;
    if(manaJucator[i].suita=='H') fSuita[1]++;
    if(manaJucator[i].suita=='D') fSuita[2]++;
    if(manaJucator[i].suita=='C') fSuita[3]++;
    }
    int pereche=0, trei=0, patru=0;

    for(int v=2; v<=14; v++){
        if(fValoare[v]==2) pereche++;
        if(fValoare[v]==3) trei++;
        if(fValoare[v]==4) patru++;
    }

    int flush=0;
        for(int i=0; i<4; i++){
    if(fSuita[i]==5) flush=1; // toate cărțile aceeași suită
    }
    int consecutiv=0;
    for(int v=2; v<=10; v++){ // verificăm 2..10 pentru 5 cărți consecutive
        if(fValoare[v] && fValoare[v+1] && fValoare[v+2] &&
            fValoare[v+3] && fValoare[v+4]){
            consecutiv=1;
            break;
            }
    }
// Pt chinta: A,2,3,4,5
    if(fValoare[14] && fValoare[2] && fValoare[3] && fValoare[4] && fValoare[5])
        consecutiv=1;

    if(consecutiv && flush){
    printf("Chinta cu culoare!\n");
    } else if(patru==1){
    printf("Patru de acelasi fel!\n");
    } else if(trei==1 && pereche==1){
    printf("Full House!\n");
    } else if(flush){
    printf("Flush!\n");
    } else if(consecutiv){
    printf("Chinta!\n");
    } else if(trei==1){
    printf("Trei de acelasi fel!\n");
    } else if(pereche==2){
    printf("Doua perechi!\n");
    } else if(pereche==1){
    printf("O pereche!\n");
    }else printf("Pachetul nu formeaza nicio pereche.\n");

    Carte manaDealer[6];
        int d=7;
        for(int i=0; i<5; i++){
            manaDealer[i] = pachet[d++]; // urmatoarele 5 cărți din pachetul amestecat
            }

    afiseazaMana(manaDealer);

    printf("Cartile dealerului: %s%s  %s%s  %s%s  %s%s  %s%s\n",simbolSuita(manaDealer[0].suita),simbolValoare(manaDealer[0].valoare),simbolSuita(manaDealer[1].suita),simbolValoare(manaDealer[1].valoare),simbolSuita(manaDealer[2].suita),simbolValoare(manaDealer[2].valoare),simbolSuita(manaDealer[3].suita),simbolValoare(manaDealer[3].valoare),simbolSuita(manaDealer[4].suita),simbolValoare(manaDealer[4].valoare));

    int dfValoare[15]={0};
    int dfSuita[4]={0};

    for(int i=0; i<5; i++){
    dfValoare[manaDealer[i].valoare]++;
    if(manaDealer[i].suita=='S') dfSuita[0]++;
    if(manaDealer[i].suita=='H') dfSuita[1]++;
    if(manaDealer[i].suita=='D') dfSuita[2]++;
    if(manaDealer[i].suita=='C') dfSuita[3]++;
    }
    int dpereche=0, dtrei=0, dpatru=0;

    for(int v=2; v<=14; v++){
        if(dfValoare[v]==2) dpereche++;
        if(dfValoare[v]==3) dtrei++;
        if(dfValoare[v]==4) dpatru++;
    }

    int dflush=0;
        for(int i=0; i<4; i++){
    if(dfSuita[i]==5) dflush=1; // toate cărțile aceeași suită
    }
    int dconsecutiv=0;
    for(int v=2; v<=10; v++){ // verificăm 2..10 pentru 5 cărți consecutive
        if(dfValoare[v] && dfValoare[v+1] && dfValoare[v+2] &&
            dfValoare[v+3] && dfValoare[v+4]){
            dconsecutiv=1;
            break;
            }
    }
// Pt chinta: A,2,3,4,5
    if(dfValoare[14] && dfValoare[2] && dfValoare[3] && dfValoare[4] && dfValoare[5])
        dconsecutiv=1;

    if(dconsecutiv && dflush){
    printf("Dealerul are Chinta cu culoare!\n");
    } else if(dpatru==1){
    printf("Dealerul are Patru de acelasi fel!\n");
    } else if(dtrei==1 && dpereche==1){
    printf("Dealerul are Full House!\n");
    } else if(dflush){
    printf("Dealerul are Flush!\n");
    } else if(dconsecutiv){
    printf("Dealerul are Chinta!\n");
    } else if(dtrei==1){
    printf("Dealerul are Trei de acelasi fel!\n");
    } else if(dpereche==2){
    printf("Dealerul are Doua perechi!\n");
    } else if(dpereche==1){
    printf("Dealerul are O pereche!\n");
    }else printf("Dealerul nu formeaza nicio pereche.\n");

    if(consecutiv && flush) { // Chintă cu culoare
        if(dconsecutiv && dflush){
            printf("Egalitate: ambele Chinta cu culoare!\n");
        } else {
            printf("Jucatorul castiga: Chinta cu culoare!\n");
        }
    }
    else if(patru==1) { // Patru de acelasi fel
        if(dpatru==1){
            printf("Egalitate: ambele Patru de acelasi fel!\n");
        } else {
            printf("Jucatorul castiga: Patru de acelasi fel!\n");
        }
    }
    else if(trei==1 && pereche==1){ // Full House
        if(dtrei==1 && dpereche==1){
            printf("Egalitate: ambele Full House!\n");
        } else {
            printf("Jucatorul castiga: Full House!\n");
        }
    }
    else if(flush) { // Flush
        if(dflush){
            printf("Egalitate: ambele Flush!\n");
        } else {
            printf("Jucatorul castiga: Flush!\n");
        }
    }
    else if(consecutiv) { // Chintă
        if(dconsecutiv){
            printf("Egalitate: ambele Chinta!\n");
        } else {
            printf("Jucatorul castiga: Chinta!\n");
        }
    }
    else if(trei==1){ // Trei de acelasi fel
        if(dtrei==1){
            printf("Egalitate: ambele Trei de acelasi fel!\n");
        } else {
            printf("Jucatorul castiga: Trei de acelasi fel!\n");
        }
    }
    else if(pereche==2){ // Doua perechi
        if(dpereche==2){
            printf("Egalitate: ambele Doua perechi!\n");
        } else {
            printf("Jucatorul castiga: Doua perechi!\n");
        }
    }
    else if(pereche==1){ // O pereche
        if(dpereche==1){
            printf("Egalitate: ambele O pereche!\n");
        } else {
            printf("Jucatorul castiga: O pereche!\n");
        }
    }
    else { // verificare dealer
    if(dconsecutiv && dflush){
        printf("Dealerul castiga: Chinta cu culoare!\n");
    }
    else if(dpatru==1){
        printf("Dealerul castiga: Patru de acelasi fel!\n");
    }
    else if(dtrei==1 && dpereche==1){
        printf("Dealerul castiga: Full House!\n");
    }
    else if(dflush){
        printf("Dealerul castiga: Flush!\n");
    }
    else if(dconsecutiv){
        printf("Dealerul castiga: Chinta!\n");
    }
    else if(dtrei==1){
        printf("Dealerul castiga: Trei de acelasi fel!\n");
    }
    else if(dpereche==2){
        printf("Dealerul castiga: Doua perechi!\n");
    }
    else if(dpereche==1){
        printf("Dealerul castiga: O pereche!\n");
    }
    else {
        printf("Egalitate\n");
    }
}


}



