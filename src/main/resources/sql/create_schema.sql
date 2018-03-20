CREATE TABLE magic_set(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR NOT NULL,
	code VARCHAR UNIQUE NOT NULL,
	release_date DATE NULL -- Not all sets have a release date
);
CREATE UNIQUE INDEX ON magic_set(lower(code));

CREATE TABLE magic_card(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR NOT NULL -- The longest card (unset longest name elemental) has 141 characters
);
CREATE UNIQUE INDEX ON magic_card(lower(name));

CREATE TABLE magic_printing(
	scryfall_id UUID PRIMARY KEY NOT NULL,
	card_id INT NOT NULL REFERENCES magic_card(id),
	set_id INT NOT NULL REFERENCES magic_set(id),
	image_uri TEXT NOT NULL
);

/*
	Bulk loads many sets in a single function call.
*/
CREATE FUNCTION bulk_load_sets(names TEXT[], codes TEXT[], release_dates DATE[]) RETURNS VOID
	AS $$ BEGIN
		INSERT INTO magic_set(name, code, release_date)
		SELECT name, code, release_date
		FROM unnest(names, codes, release_dates) new_sets(name, code, release_date)
		ON CONFLICT (code) DO UPDATE SET name = excluded.name, release_date = excluded.release_date
		;
	END $$ LANGUAGE 'plpgsql';


/*
	Bulk load many cards in a single function call.
*/
CREATE FUNCTION bulk_load_cards(names TEXT[]) RETURNS VOID
	AS $$ BEGIN
		INSERT INTO magic_card(name)
		SELECT name
		FROM unnest(names) new_cards(name)
		ON CONFLICT DO NOTHING;
	END $$ LANGUAGE 'plpgsql';


CREATE FUNCTION bulk_load_printings(scryfall_ids UUID[], card_names TEXT[], set_codes TEXT[], image_uris TEXT[]) RETURNS VOID
	AS $$ BEGIN
		INSERT INTO magic_printing(scryfall_id, card_id, set_id, image_uri)
		SELECT new_printings.scryfall_id, mc.id, ms.id, new_printings.image_uri
		FROM unnest(scryfall_ids, card_names, set_codes, image_uris) new_printings(scryfall_id, card_name, set_code, image_uri)
		INNER JOIN magic_card mc ON mc.name = new_printings.card_name
		INNER JOIN magic_set ms ON ms.code = new_printings.set_code
		ON CONFLICT (scryfall_id) DO UPDATE SET card_id = excluded.card_id, set_id = excluded.set_id, image_uri = excluded.image_uri
		;
	END $$ LANGUAGE 'plpgsql';