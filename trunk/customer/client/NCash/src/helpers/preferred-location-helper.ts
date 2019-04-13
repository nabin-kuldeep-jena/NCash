import {CountryTbl} from'../common/country-tbl'
import {State} from'../common/state'
import {CityTbl} from'../common/city-tbl'

export class PreferredLocationHelper{
    static preferredLocationHelper: PreferredLocationHelper = null;

    static getInstance() {
        if (this.preferredLocationHelper == null)
            this.preferredLocationHelper = new PreferredLocationHelper();
        return this.preferredLocationHelper;
    }

    initialiseCountries(data: any,countries: CountryTbl[]) {
        var countriesFromResponse = JSON.parse(data._body);
        for (var index in countriesFromResponse) {
            let country: CountryTbl = new CountryTbl();
            country.ctrName = countriesFromResponse[index].properties.ctrName;
            country.ctrId = countriesFromResponse[index].id;
            country.ctrDialCode = countriesFromResponse[index].properties.ctrDialCode
            country.ctrMaxMobileNoLength = countriesFromResponse[index].properties.ctrMaxMobileNoLength
            countries[index] = country;
        }
    }

    initialiseStates(data: any, states: State[]) {
         //states = new Array<State>();
         //this.cities=new Array<CityTbl>();
         //this.isCityDisabled=true;
         var statesFromResponse = JSON.parse(data._body);
         for (var index in statesFromResponse) {
             let state: State = new State();
             state.steName = statesFromResponse[index].properties.steName;
             state.steId = statesFromResponse[index].id;
             states[index] = state;
         }
    }

    initialiseCities(data: any, cities: CityTbl[]) {
        //this.cities = new Array<CityTbl>();
        var citiesFromResponse = JSON.parse(data._body);
        for (var index in citiesFromResponse) {
            let city: CityTbl = new CityTbl();
            city.ctyName = citiesFromResponse[index].properties.ctyName;
            city.ctyId = citiesFromResponse[index].id;
            cities[index] = city;
        }
    }
}