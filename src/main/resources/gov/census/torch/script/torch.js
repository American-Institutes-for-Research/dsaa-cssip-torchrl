importPackage(Packages.gov.census.torch); 
importPackage(Packages.gov.census.torch.comparators);
importPackage(Packages.gov.census.torch.counter);
importPackage(Packages.gov.census.torch.matcher);
importPackage(Packages.gov.census.torch.model);
importPackage(Packages.gov.census.torch.io);

EXACT = StandardComparators.EXACT;
PRORATED = StandardComparators.PRORATED;
YEAR = StandardComparators.YEAR;
STRING = StandardComparators.STRING;

GLOBAL_NAMES = new java.util.HashSet([
        "javax.script.filename", "GLOBAL_NAMES",
        "context", "print", "println", "null", "tv4",
        "Script", "ScriptSchema", "StandardComparators", "ls",
        "EXACT", "PRORATED", "YEAR", "STRING"
]);

function ls() {
    var ary = [];

    for (var name in this) {
        if (!GLOBAL_NAMES.contains(name) && name.substring(0, 2) !== "__")
            ary.push(name);
    }

    ary.sort();
    return JSON.stringify(ary);
}

function __isDefined(x) {
    return (typeof x != 'undefined');
}

function __readFile(file) {
    var s = "";
    try {
        var rdr = new java.io.BufferedReader(new java.io.FileReader(file));

        var line = "";
        while ((line = rdr.readLine()) != null) {
            s += line + "\n";
        }
    }
    catch (e) {
        throw "There was a problem reading the file: " + file;
    }
    finally {
        if (rdr != null)
            rdr.close();
    }

    return s;
}

function __reflect(obj, name) {
    var f = obj.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.get(obj);
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
    run: function(file) {
        var s = __readFile(file);
        return eval(s);
    },

    load: function(file) {
        var s = __readFile(file);
        return JSON.parse(s);
    },

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
        if (__isDefined(blockingFields)) {
            for (var i = 0; i < blockingFields.length; i++) {
                b.blockingField(blockingFields[i]);
            }
        }

        var idFields = obj['idFields'];
        if (__isDefined(idFields)) {
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
        if (__isDefined(blockingFields)) {
            for (var i = 0; i < blockingFields.length; i++) {
                b.blockingField(blockingFields[i]);
            }
        }

        var idFields = obj['idFields'];
        if (__isDefined(idFields)) {
            for (var i = 0; i < idFields.length; i++) {
                b.idField(idFields[i]);
            }
        }

        var d = obj['delimiter'];
        if (__isDefined(d))
            b.delimiter(d);

        var h = obj['header'];
        if (__isDefined(h))
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
        if (__isDefined(h))
            b.handleBlanks(h);

        var ary = obj['compare'];
        for (var i = 0; i < ary.length; i++) {
            c = ary[i];
            b.compare(c[0], c[1]);
        }

        return b.build();
    },

    save: function(obj, file) {
        var result = {}

        if (obj instanceof FixedWidthFileSchema) {
            var columnStart = __reflect(obj, "_columnStart");
            var columnOff = __reflect(obj, "_columnOff");
            var rs = obj.schema();
            var columns = __reflect(rs, "_columns");

            result.columns = [];
            for (var i = 0; i < columns.length; i++) {
                var thisColumn = [String(columns[i]), columnStart[i], columnOff[i]]
                result.columns.push(thisColumn);
            }

            var blockingFields = __reflect(rs, "_blockingFields");
            result.blockingFields = [];
            for (var i = 0; i < blockingFields.length; i++) {
                result.blockingFields.push(String(blockingFields[i]));
            }

            if (rs.hasId()) {
                var idFields = __reflect(rs, "_idFields");
                result.idFields = [];
                for (var i = 0; i < idFields.length; i++) {
                    result.idFields.push(String(idFields[i]));
                }
            }
        } else if (obj instanceof DelimitedFileSchema) {
            var rs = obj.schema();
            var columns = __reflect(rs, "_columns");
            result.columns = [];
            for (var i = 0; i < columns.length; i++) {
                result.columns.push(String(columns[i]));
            }

            var blockingFields = __reflect(rs, "_blockingFields");
            result.blockingFields = [];
            for (var i = 0; i < blockingFields.length; i++) {
                result.blockingFields.push(String(blockingFields[i]));
            }

            if (rs.hasId()) {
                var idFields = __reflect(rs, "_idFields");
                result.idFields = [];
                for (var i = 0; i < idFields.length; i++) {
                    result.idFields.push(String(idFields[i]));
                }
            }

            var strategy = __reflect(obj, "_strategy");
            result.delimiter = 
                String(java.lang.Character.toString(strategy.getDelimiter()));
            result.header = Boolean(strategy.isSkipHeader());
        } else {
            throw "Don't know how to save this type of object";
        }

        try {
            var writer = new java.io.PrintWriter(new java.io.FileWriter(file));
            writer.println(JSON.stringify(result));
            writer.flush();
        }
        catch (e) {
            throw "There was a problem writing the file: " + file;
        }
        finally {
            if (writer != null)
                writer.close();
        }
    }
}
