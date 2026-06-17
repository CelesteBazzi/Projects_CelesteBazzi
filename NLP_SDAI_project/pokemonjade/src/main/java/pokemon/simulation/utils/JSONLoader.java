package pokemon.simulation.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import pokemon.simulation.PokemonCard;

public class JSONLoader {
    public static List<PokemonCard> loadCards(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(new File(path), PokemonCard[].class));
    }
}
