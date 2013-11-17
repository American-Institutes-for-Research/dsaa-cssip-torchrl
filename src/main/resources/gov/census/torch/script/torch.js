importPackage(Packages.gov.census.torch); importPackage(Packages.gov.census.torch.comparators);
importPackage(Packages.gov.census.torch.counter);
importPackage(Packages.gov.census.torch.matcher);
importPackage(Packages.gov.census.torch.model);
importPackage(Packages.gov.census.torch.io);

EXACT = StandardComparators.EXACT;
PRORATED = StandardComparators.PRORATED;
YEAR = StandardComparators.YEAR;
STRING = StandardComparators.STRING;

function isDefined(x) {
    return (typeof x != 'undefined');
}

ScriptSchema = {
    newFixedWidthFileSchema: {
        "$schema": "http://json-schema.org/draft-04/schema#",
        "type": "object",
        "properties": {
            "columns": {
                "type": "array",
                "items": {
                    "type": "array",
                    "items": [
                        {"type": "string" }, 
                        {"type": "integer"}, 
                        {"type": "integer"}
                    ]
                } 
            },
            "blockingFields": {
                "type": "array",
                "items": {"type": "string"}
            },
            "idFields": {
                "type": "array",
                "items": {"type": "string"}
            },
        },
        "required": ["columns"]
    },

    newDelimitedFileSchema: {
        "$schema": "http://json-schema.org/draft-04/schema#",
        "type": "object",
        "properties": {
            "columns": {
                "type": "array",
                "items": {"type": "string"} 
            },
            "blockingFields": {
                "type": "array",
                "items": {"type": "string"}
            },
            "idFields": {
                "type": "array",
                "items": {"type": "string"}
            },
            "delimiter": {"type": "string"},
            "header": {"type": "boolean"}
        },
        "required": ["columns"]
    },

    newRecordComparator: {
        "$schema": "http://json-schema.org/draft-04/schema#",
        "type": "object",
        "properties": {
            "schema": {
                "anyOf": [
                    {"type": "object"}, 
                    {"type": "array", "items": {"type": "object"}}
                ]
            },
            "compare": {
                "type": "array",
                "items": {
                    "type": "array",
                    "items": [
                        {"type": "string"},
                        {"type": "object"}
                    ]
                }
            },
            "handleBlanks": {"type": "boolean"}
        },
        "required": ["schema", "compare"]
    }
};

Script = {

    newFixedWidthFileSchema: function(obj) {
        if (!tv4.validate(obj, ScriptSchema.newFixedWidthFileSchema)) {
            throw tv4.error.message;
        }

        var b = new FixedWidthFileSchema.Builder();

        var columns = obj['columns'];
        for (var i = 0; i < columns.length; i++) {
            var f = columns[i];
            b.column(f[0], f[1], f[2]);
        }

        var blockingFields = obj['blockingFields'];
        if (isDefined(blockingFields)) {
            for (var i = 0; i < blockingFields.length; i++) {
                b.blockingField(blockingFields[i]);
            }
        }

        var idFields = obj['idFields'];
        if (isDefined(idFields)) {
            for (var i = 0; i < idFields.length; i++) {
                b.idField(idFields[i]);
            }
        }

        return b.build();
    },

    newDelimitedFileSchema: function(obj) {
        if (!tv4.validate(obj, ScriptSchema.newDelimitedFileSchema)) {
            throw tv4.error.message;
        }
        var b = new DelimitedFileSchema.Builder();

        var columns = obj['columns'];
        for (var i = 0; i < columns.length; i++) {
            b.column(columns[i]);
        }

        var blockingFields = obj['blockingFields'];
        if (isDefined(blockingFields)) {
            for (var i = 0; i < blockingFields.length; i++) {
                b.blockingField(blockingFields[i]);
            }
        }

        var idFields = obj['idFields'];
        if (isDefined(idFields)) {
            for (var i = 0; i < idFields.length; i++) {
                b.idField(idFields[i]);
            }
        }

        var d = obj['delimiter'];
        if (isDefined(d))
            b.delimiter(d);

        var h = obj['header'];
        if (isDefined(h))
            b.header(h);

        return b.build();
    },

    newRecordComparator: function(obj) {
        if (!tv4.validate(obj, ScriptSchema.newRecordComparator)) {
            throw tv4.error.message;
        }

        var s = obj['schema'];
        var schema1, schema2;
        if (s instanceof Array) {
            schema1 = s[0];
            schema2 = s[1];
        } else {
            schema1 = s;
            schema2 = s;
        }

        var b = new RecordComparator.Builder(schema1, schema2);

        var handleBlanks = true;
        var h = obj['handleBlanks'];
        if (isDefined(h))
            b.handleBlanks(h);

        var ary = obj['compare'];
        for (var i = 0; i < ary.length; i++) {
            c = ary[i];
            b.compare(c[0], c[1]);
        }

        return b.build();
    }
}

$ = Script;
