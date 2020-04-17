using System;
using System.ComponentModel.DataAnnotations;
using System.Runtime.Serialization;
using WebApiObjetos.Models.Entities;

namespace WebApiObjetos.Domain
{
    [DataContract()]
    public class LocationDTO
    {
        [DataMember()]
        public int Id { get; set; }
        [DataMember()]
        public int UserId { get; set; }
        [DataMember()]
        public string Tag { get; set; }
        [DataMember()]
        [Required(ErrorMessage = "Color cannot be empty")]
        public int Color { get; set; }
        [DataMember()]
        [Required(ErrorMessage = "Coordinates cannot be empty")]
        public string Coordinates { get; set; }
        [DataMember()]
        [Required(ErrorMessage = "Type cannot be empty")]
        public short Type { get; set; }

        [DataMember]
        public Nullable<int> ImageId { get; set; }

        [DataMember]
        public bool IsSearch { get; set; }

        [DataMember]
        public bool IsFound { get; set; }

        [DataMember]
        public DateTime InsertDate { get; set; }

        public Location ToEntity()
        {
            return new Location
            {
                Id= this.Id,
                UserId = this.UserId,
                Tag = this.Tag,
                Color = this.Color,
                Coordinates = this.Coordinates,
                Type = this.Type,
                ImageId = this.ImageId,
                IsSearch = this.IsSearch,
                IsFound = this.IsFound,
                InsertDate = this.InsertDate
            };

        }

    }
}
